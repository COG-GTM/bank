package org.mounanga.collectionsservice.service.implementation;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mounanga.collectionsservice.dto.AgentAssignmentRequestDTO;
import org.mounanga.collectionsservice.dto.CollectionActionRequestDTO;
import org.mounanga.collectionsservice.dto.CollectionActionResponseDTO;
import org.mounanga.collectionsservice.dto.DelinquencySummaryDTO;
import org.mounanga.collectionsservice.dto.DelinquentAccountPageResponseDTO;
import org.mounanga.collectionsservice.dto.DelinquentAccountRequestDTO;
import org.mounanga.collectionsservice.dto.DelinquentAccountResponseDTO;
import org.mounanga.collectionsservice.dto.StatusUpdateRequestDTO;
import org.mounanga.collectionsservice.entity.CollectionAction;
import org.mounanga.collectionsservice.entity.DelinquentAccount;
import org.mounanga.collectionsservice.enums.CollectionStatus;
import org.mounanga.collectionsservice.enums.DelinquencyBucket;
import org.mounanga.collectionsservice.enums.ProductType;
import org.mounanga.collectionsservice.enums.Region;
import org.mounanga.collectionsservice.event.AgentAssignedEvent;
import org.mounanga.collectionsservice.event.CollectionActionRecordedEvent;
import org.mounanga.collectionsservice.event.DelinquentAccountStatusChangedEvent;
import org.mounanga.collectionsservice.exception.DelinquentAccountNotFoundException;
import org.mounanga.collectionsservice.exception.FieldValidationException;
import org.mounanga.collectionsservice.repository.CollectionActionRepository;
import org.mounanga.collectionsservice.repository.DelinquentAccountRepository;
import org.mounanga.collectionsservice.service.CollectionsService;
import org.mounanga.collectionsservice.util.mappers.Mapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CollectionsServiceImpl implements CollectionsService {

    private final DelinquentAccountRepository delinquentAccountRepository;
    private final CollectionActionRepository collectionActionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CollectionsServiceImpl(DelinquentAccountRepository delinquentAccountRepository,
                                  CollectionActionRepository collectionActionRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this.delinquentAccountRepository = delinquentAccountRepository;
        this.collectionActionRepository = collectionActionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DelinquentAccountResponseDTO getDelinquentAccountById(String id) {
        log.info("In getDelinquentAccountById()");
        DelinquentAccount account = findById(id);
        return Mapper.fromDelinquentAccount(account);
    }

    @Override
    public DelinquentAccountPageResponseDTO searchDelinquentAccounts(DelinquencyBucket bucket,
                                                                     ProductType productType,
                                                                     Region region,
                                                                     CollectionStatus status,
                                                                     int page,
                                                                     int size) {
        log.info("In searchDelinquentAccounts()");
        Pageable pageable = PageRequest.of(page, size);
        Page<DelinquentAccount> result = delinquentAccountRepository.searchDelinquentAccounts(bucket, productType, region, status, pageable);
        log.info("{} delinquent accounts found", result.getTotalElements());
        return Mapper.fromPageOfDelinquentAccounts(result);
    }

    @Transactional
    @Override
    public DelinquentAccountResponseDTO createDelinquentAccount(@NotNull DelinquentAccountRequestDTO dto) {
        log.info("In createDelinquentAccount()");
        validateAccountNumberAvailable(dto.getAccountNumber());
        DelinquentAccount account = Mapper.fromDelinquentAccount(dto);
        DelinquentAccount saved = delinquentAccountRepository.save(account);
        log.info("Delinquent account created with id {}", saved.getId());
        return Mapper.fromDelinquentAccount(saved);
    }

    @Transactional
    @Override
    public DelinquentAccountResponseDTO updateStatus(String id, @NotNull StatusUpdateRequestDTO dto) {
        log.info("In updateStatus()");
        DelinquentAccount account = findById(id);
        CollectionStatus previous = account.getStatus();
        if (previous == dto.getStatus()) {
            log.info("No status change for account '{}': already {}", id, previous);
            return Mapper.fromDelinquentAccount(account);
        }
        account.setStatus(dto.getStatus());
        DelinquentAccount saved = delinquentAccountRepository.save(account);
        log.info("Delinquent account '{}' status: {} -> {}", id, previous, dto.getStatus());
        eventPublisher.publishEvent(new DelinquentAccountStatusChangedEvent(
                this,
                saved.getId(),
                saved.getAccountNumber(),
                saved.getAccountHolderEmail(),
                previous,
                saved.getStatus(),
                dto.getReason(),
                LocalDateTime.now()
        ));
        return Mapper.fromDelinquentAccount(saved);
    }

    @Transactional
    @Override
    public DelinquentAccountResponseDTO assignAgent(String id, @NotNull AgentAssignmentRequestDTO dto) {
        log.info("In assignAgent()");
        DelinquentAccount account = findById(id);
        LocalDateTime now = LocalDateTime.now();
        account.setAssignedAgentId(dto.getAgentId());
        account.setAssignedAt(now);
        CollectionStatus previous = account.getStatus();
        if (previous == CollectionStatus.NEW) {
            account.setStatus(CollectionStatus.IN_PROGRESS);
        }
        DelinquentAccount saved = delinquentAccountRepository.save(account);
        log.info("Agent '{}' assigned to delinquent account '{}'", dto.getAgentId(), id);
        eventPublisher.publishEvent(new AgentAssignedEvent(
                this,
                saved.getId(),
                saved.getAccountNumber(),
                saved.getAccountHolderEmail(),
                saved.getAssignedAgentId(),
                now
        ));
        if (saved.getStatus() != previous) {
            eventPublisher.publishEvent(new DelinquentAccountStatusChangedEvent(
                    this,
                    saved.getId(),
                    saved.getAccountNumber(),
                    saved.getAccountHolderEmail(),
                    previous,
                    saved.getStatus(),
                    "Agent assigned",
                    now
            ));
        }
        return Mapper.fromDelinquentAccount(saved);
    }

    @Transactional
    @Override
    public CollectionActionResponseDTO recordAction(String id, @NotNull CollectionActionRequestDTO dto) {
        log.info("In recordAction()");
        DelinquentAccount account = findById(id);
        CollectionAction action = Mapper.fromCollectionAction(dto);
        account.addAction(action);
        CollectionAction savedAction = collectionActionRepository.save(action);
        log.info("Collection action '{}' recorded for delinquent account '{}'", savedAction.getId(), id);
        eventPublisher.publishEvent(new CollectionActionRecordedEvent(
                this,
                account.getId(),
                account.getAccountNumber(),
                account.getAccountHolderEmail(),
                savedAction.getType(),
                savedAction.getOutcome(),
                savedAction.getPerformedByAgentId(),
                savedAction.getPerformedAt()
        ));
        return Mapper.fromCollectionAction(savedAction);
    }

    @Override
    public List<CollectionActionResponseDTO> getActions(String id) {
        log.info("In getActions()");
        findById(id);
        List<CollectionAction> actions = collectionActionRepository.findByDelinquentAccountIdOrderByPerformedAtDesc(id);
        return Mapper.fromListOfCollectionActions(actions);
    }

    @Override
    public List<DelinquencySummaryDTO> getDelinquencySummary() {
        log.info("In getDelinquencySummary()");
        Map<DelinquencyBucket, DelinquencySummaryDTO> byBucket = new EnumMap<>(DelinquencyBucket.class);
        for (DelinquencyBucket bucket : DelinquencyBucket.values()) {
            byBucket.put(bucket, DelinquencySummaryDTO.builder()
                    .bucket(bucket)
                    .accountCount(0L)
                    .totalOutstandingBalance(BigDecimal.ZERO)
                    .build());
        }
        for (DelinquentAccountRepository.DelinquencySummaryProjection row : delinquentAccountRepository.aggregateByBucket()) {
            byBucket.put(row.getBucket(), DelinquencySummaryDTO.builder()
                    .bucket(row.getBucket())
                    .accountCount(row.getAccountCount() == null ? 0L : row.getAccountCount())
                    .totalOutstandingBalance(row.getTotalBalance() == null ? BigDecimal.ZERO : row.getTotalBalance())
                    .build());
        }
        List<DelinquencySummaryDTO> result = new ArrayList<>(byBucket.values());
        Collections.sort(result, (a, b) -> a.getBucket().ordinal() - b.getBucket().ordinal());
        return result;
    }

    private DelinquentAccount findById(String id) {
        return delinquentAccountRepository.findById(id)
                .orElseThrow(() -> new DelinquentAccountNotFoundException(String.format("Delinquent account with id '%s' not found", id)));
    }

    private void validateAccountNumberAvailable(String accountNumber) {
        if (delinquentAccountRepository.existsByAccountNumber(accountNumber)) {
            throw new FieldValidationException("Invalid data",
                    List.of(String.format("Delinquent account with accountNumber '%s' already exists", accountNumber)));
        }
    }
}
