package org.mounanga.collectionsservice.service.implementation;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mounanga.collectionsservice.dto.*;
import org.mounanga.collectionsservice.entity.CollectionAction;
import org.mounanga.collectionsservice.entity.DelinquentAccount;
import org.mounanga.collectionsservice.enums.CollectionStatus;
import org.mounanga.collectionsservice.enums.DelinquencyBucket;
import org.mounanga.collectionsservice.enums.ProductType;
import org.mounanga.collectionsservice.enums.Region;
import org.mounanga.collectionsservice.event.CollectionEventPublisher;
import org.mounanga.collectionsservice.repository.CollectionActionRepository;
import org.mounanga.collectionsservice.repository.DelinquentAccountRepository;
import org.mounanga.collectionsservice.service.CollectionsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class CollectionsServiceImpl implements CollectionsService {

    private final DelinquentAccountRepository delinquentAccountRepository;
    private final CollectionActionRepository collectionActionRepository;
    private final CollectionEventPublisher eventPublisher;

    public CollectionsServiceImpl(DelinquentAccountRepository delinquentAccountRepository,
                                  CollectionActionRepository collectionActionRepository,
                                  CollectionEventPublisher eventPublisher) {
        this.delinquentAccountRepository = delinquentAccountRepository;
        this.collectionActionRepository = collectionActionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DelinquentAccountPageResponseDTO getDelinquentAccounts(
            DelinquencyBucket bucket,
            ProductType productType,
            Region region,
            CollectionStatus status,
            int page,
            int size) {
        log.info("In getDelinquentAccounts() with filters - bucket: {}, productType: {}, region: {}, status: {}",
                bucket, productType, region, status);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "outstandingBalance"));
        Page<DelinquentAccount> accounts = delinquentAccountRepository.findByFilters(bucket, productType, region, status, pageable);
        log.info("{} delinquent accounts found", accounts.getTotalElements());
        return Mapper.fromPageOfDelinquentAccounts(accounts);
    }

    @Override
    public DelinquentAccountResponseDTO getDelinquentAccountById(String id) {
        log.info("In getDelinquentAccountById()");
        DelinquentAccount account = findDelinquentAccountById(id);
        log.info("Delinquent account with id '{}' found", id);
        return Mapper.fromDelinquentAccount(account);
    }

    @Override
    public DelinquentAccountResponseDTO getDelinquentAccountByAccountId(String accountId) {
        log.info("In getDelinquentAccountByAccountId()");
        DelinquentAccount account = delinquentAccountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new DelinquentAccountNotFoundException(
                        String.format("Delinquent account with accountId '%s' not found", accountId)));
        log.info("Delinquent account with accountId '{}' found", accountId);
        return Mapper.fromDelinquentAccount(account);
    }

    @Override
    public List<DelinquentAccountResponseDTO> getDelinquentAccountsByCustomerId(String customerId) {
        log.info("In getDelinquentAccountsByCustomerId()");
        List<DelinquentAccount> accounts = delinquentAccountRepository.findByCustomerId(customerId);
        log.info("{} delinquent accounts found for customer '{}'", accounts.size(), customerId);
        return Mapper.fromDelinquentAccounts(accounts);
    }

    @Override
    public DelinquentAccountPageResponseDTO searchDelinquentAccounts(String keyword, int page, int size) {
        log.info("In searchDelinquentAccounts()");
        Pageable pageable = PageRequest.of(page, size);
        Page<DelinquentAccount> accounts = delinquentAccountRepository.search("%" + keyword + "%", pageable);
        log.info("{} delinquent accounts found for keyword '{}'", accounts.getTotalElements(), keyword);
        return Mapper.fromPageOfDelinquentAccounts(accounts);
    }

    @Override
    public List<DelinquencySummaryDTO> getDelinquencySummary() {
        log.info("In getDelinquencySummary()");
        List<Object[]> results = delinquentAccountRepository.getDelinquencySummary();
        return results.stream().map(row -> DelinquencySummaryDTO.builder()
                .bucket((DelinquencyBucket) row[0])
                .accountCount((Long) row[1])
                .totalOutstandingBalance((BigDecimal) row[2])
                .build()).toList();
    }

    @Transactional
    @Override
    public DelinquentAccountResponseDTO updateCollectionStatus(String id, CollectionStatus status) {
        log.info("In updateCollectionStatus() - id: {}, status: {}", id, status);
        DelinquentAccount account = findDelinquentAccountById(id);
        CollectionStatus previousStatus = account.getCollectionStatus();
        account.setCollectionStatus(status);
        DelinquentAccount updated = delinquentAccountRepository.save(account);
        log.info("Collection status updated from {} to {} for account '{}'", previousStatus, status, id);
        eventPublisher.publishStatusChanged(updated, previousStatus);
        return Mapper.fromDelinquentAccount(updated);
    }

    @Transactional
    @Override
    public DelinquentAccountResponseDTO assignAgent(String id, String agentId) {
        log.info("In assignAgent() - id: {}, agentId: {}", id, agentId);
        DelinquentAccount account = findDelinquentAccountById(id);
        account.setAssignedAgentId(agentId);
        if (account.getCollectionStatus() == CollectionStatus.PENDING) {
            account.setCollectionStatus(CollectionStatus.IN_PROGRESS);
        }
        DelinquentAccount updated = delinquentAccountRepository.save(account);
        log.info("Agent '{}' assigned to delinquent account '{}'", agentId, id);
        eventPublisher.publishAgentAssigned(updated, agentId);
        return Mapper.fromDelinquentAccount(updated);
    }

    @Transactional
    @Override
    public CollectionActionResponseDTO addCollectionAction(String delinquentAccountId, @NotNull CollectionActionRequestDTO request) {
        log.info("In addCollectionAction() - delinquentAccountId: {}", delinquentAccountId);
        DelinquentAccount account = findDelinquentAccountById(delinquentAccountId);
        CollectionAction action = CollectionAction.builder()
                .actionType(request.getActionType())
                .description(request.getDescription())
                .performedBy(account.getAssignedAgentId() != null ? account.getAssignedAgentId() : "SYSTEM")
                .actionDate(request.getActionDate())
                .outcome(request.getOutcome())
                .followUpDate(request.getFollowUpDate())
                .delinquentAccount(account)
                .build();
        CollectionAction saved = collectionActionRepository.save(action);
        log.info("Collection action '{}' added for account '{}'", saved.getId(), delinquentAccountId);
        eventPublisher.publishActionRecorded(account, saved);
        return Mapper.fromCollectionAction(saved);
    }

    @Override
    public List<CollectionActionResponseDTO> getCollectionActions(String delinquentAccountId, int page, int size) {
        log.info("In getCollectionActions() - delinquentAccountId: {}", delinquentAccountId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "actionDate"));
        Page<CollectionAction> actions = collectionActionRepository.findByDelinquentAccountId(delinquentAccountId, pageable);
        log.info("{} collection actions found for account '{}'", actions.getTotalElements(), delinquentAccountId);
        return Mapper.fromCollectionActions(actions.getContent());
    }

    private DelinquentAccount findDelinquentAccountById(String id) {
        return delinquentAccountRepository.findById(id)
                .orElseThrow(() -> new DelinquentAccountNotFoundException(
                        String.format("Delinquent account with id '%s' not found", id)));
    }
}
