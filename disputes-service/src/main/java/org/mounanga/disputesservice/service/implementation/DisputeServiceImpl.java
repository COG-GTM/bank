package org.mounanga.disputesservice.service.implementation;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mounanga.disputesservice.dto.AccountResponseDTO;
import org.mounanga.disputesservice.dto.DisputePageResponseDTO;
import org.mounanga.disputesservice.dto.DisputeRequestDTO;
import org.mounanga.disputesservice.dto.DisputeResolutionRequestDTO;
import org.mounanga.disputesservice.dto.DisputeResponseDTO;
import org.mounanga.disputesservice.entity.Dispute;
import org.mounanga.disputesservice.enums.DisputeStatus;
import org.mounanga.disputesservice.exception.AccountNotFoundException;
import org.mounanga.disputesservice.exception.AccountServiceUnavailableException;
import org.mounanga.disputesservice.exception.DisputeNotFoundException;
import org.mounanga.disputesservice.exception.FieldValidationException;
import org.mounanga.disputesservice.repository.DisputeRepository;
import org.mounanga.disputesservice.service.DisputeService;
import org.mounanga.disputesservice.util.mappers.Mapper;
import org.mounanga.disputesservice.web.AccountRestClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final AccountRestClient accountRestClient;

    public DisputeServiceImpl(DisputeRepository disputeRepository, AccountRestClient accountRestClient) {
        this.disputeRepository = disputeRepository;
        this.accountRestClient = accountRestClient;
    }

    @Transactional
    @Override
    public DisputeResponseDTO openDispute(@NotNull DisputeRequestDTO dto) {
        log.info("In openDispute()");
        AccountResponseDTO account = findAccountById(dto.getAccountId());
        if (account == null) {
            throw new AccountNotFoundException(String.format("Account with id %s not found", dto.getAccountId()));
        }
        Dispute dispute = Mapper.fromDispute(dto, account);
        Dispute savedDispute = disputeRepository.save(dispute);
        log.info("Dispute opened with id {}", savedDispute.getId());
        return Mapper.fromDispute(savedDispute);
    }

    @Override
    public DisputeResponseDTO getDisputeById(String id) {
        log.info("In getDisputeById()");
        Dispute dispute = findDisputeById(id);
        log.info("Dispute with id '{}' found", id);
        return Mapper.fromDispute(dispute);
    }

    @Override
    public DisputePageResponseDTO getDisputesByAccountId(String accountId, int page, int size) {
        log.info("In getDisputesByAccountId()");
        Pageable pageable = PageRequest.of(page, size);
        Page<Dispute> disputes = disputeRepository.findByAccountIdOrderByCreatedDateDesc(accountId, pageable);
        log.info("{} disputes found for account '{}'", disputes.getTotalElements(), accountId);
        return Mapper.fromPageOfDisputes(disputes);
    }

    @Transactional
    @Override
    public DisputeResponseDTO resolveDispute(String id, @NotNull DisputeResolutionRequestDTO dto) {
        log.info("In resolveDispute()");
        Dispute dispute = findDisputeById(id);
        validationBeforeResolveDispute(dispute.getStatus(), dto.getStatus());
        Dispute disputeToUpdate = Mapper.updateDisputeItems(dispute, dto);
        Dispute disputeUpdated = disputeRepository.save(disputeToUpdate);
        log.info("Dispute with id {} moved to status {}", disputeUpdated.getId(), disputeUpdated.getStatus());
        return Mapper.fromDispute(disputeUpdated);
    }

    private Dispute findDisputeById(String id) {
        return disputeRepository.findById(id)
                .orElseThrow(() -> new DisputeNotFoundException(String.format("Dispute with id %s not found", id)));
    }

    @Nullable
    private AccountResponseDTO findAccountById(String accountId) {
        try {
            return accountRestClient.getAccountById(accountId);
        } catch (FeignException.NotFound e) {
            log.warn("Account with id '{}' not found", accountId);
            return null;
        } catch (Exception e) {
            log.error("Account lookup for id '{}' failed: {}", accountId, e.getMessage());
            throw new AccountServiceUnavailableException("Account service is unavailable, please retry later", e);
        }
    }

    private void validationBeforeResolveDispute(DisputeStatus currentStatus, DisputeStatus newStatus) {
        if (isFinal(currentStatus)) {
            throw new FieldValidationException("Invalid data",
                    List.of(String.format("Dispute is already closed with status '%s'", currentStatus)));
        }
        if (newStatus == DisputeStatus.OPEN) {
            throw new FieldValidationException("Invalid data",
                    List.of("field 'status' can not be set back to 'OPEN'"));
        }
    }

    private boolean isFinal(DisputeStatus status) {
        return status == DisputeStatus.RESOLVED_CUSTOMER
                || status == DisputeStatus.RESOLVED_MERCHANT
                || status == DisputeStatus.REJECTED;
    }
}
