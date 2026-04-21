package org.mounanga.collectionsservice.util.mappers;

import org.jetbrains.annotations.NotNull;
import org.mounanga.collectionsservice.dto.CollectionActionRequestDTO;
import org.mounanga.collectionsservice.dto.CollectionActionResponseDTO;
import org.mounanga.collectionsservice.dto.DelinquentAccountPageResponseDTO;
import org.mounanga.collectionsservice.dto.DelinquentAccountRequestDTO;
import org.mounanga.collectionsservice.dto.DelinquentAccountResponseDTO;
import org.mounanga.collectionsservice.entity.CollectionAction;
import org.mounanga.collectionsservice.entity.DelinquentAccount;
import org.mounanga.collectionsservice.enums.CollectionStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Utility class for mapping between DTOs and entities in the collections service.
 */
public final class Mapper {

    private Mapper() {
        super();
    }

    public static @NotNull DelinquentAccount fromDelinquentAccount(final @NotNull DelinquentAccountRequestDTO dto) {
        return DelinquentAccount.builder()
                .accountNumber(dto.getAccountNumber())
                .customerId(dto.getCustomerId())
                .accountHolderName(dto.getAccountHolderName())
                .accountHolderEmail(dto.getAccountHolderEmail())
                .accountHolderPhone(dto.getAccountHolderPhone())
                .productType(dto.getProductType())
                .region(dto.getRegion())
                .delinquencyBucket(dto.getDelinquencyBucket())
                .outstandingBalance(dto.getOutstandingBalance())
                .daysPastDue(dto.getDaysPastDue())
                .lastPaymentDate(dto.getLastPaymentDate())
                .status(CollectionStatus.NEW)
                .build();
    }

    public static DelinquentAccountResponseDTO fromDelinquentAccount(final @NotNull DelinquentAccount account) {
        return DelinquentAccountResponseDTO.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomerId())
                .accountHolderName(account.getAccountHolderName())
                .accountHolderEmail(account.getAccountHolderEmail())
                .accountHolderPhone(account.getAccountHolderPhone())
                .productType(account.getProductType())
                .region(account.getRegion())
                .delinquencyBucket(account.getDelinquencyBucket())
                .status(account.getStatus())
                .outstandingBalance(account.getOutstandingBalance())
                .daysPastDue(account.getDaysPastDue())
                .lastPaymentDate(account.getLastPaymentDate())
                .assignedAgentId(account.getAssignedAgentId())
                .assignedAt(account.getAssignedAt())
                .createdDate(account.getCreatedDate())
                .createdBy(account.getCreatedBy())
                .lastModifiedDate(account.getLastModifiedDate())
                .lastModifiedBy(account.getLastModifiedBy())
                .build();
    }

    public static List<DelinquentAccountResponseDTO> fromListOfDelinquentAccounts(final @NotNull List<DelinquentAccount> accounts) {
        return accounts.stream().map(Mapper::fromDelinquentAccount).toList();
    }

    public static DelinquentAccountPageResponseDTO fromPageOfDelinquentAccounts(final @NotNull Page<DelinquentAccount> page) {
        return DelinquentAccountPageResponseDTO.builder()
                .delinquentAccounts(fromListOfDelinquentAccounts(page.getContent()))
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .numberOfElements(page.getNumberOfElements())
                .number(page.getNumber())
                .hasContent(page.hasContent())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .hasPrevious(page.hasPrevious())
                .hasNext(page.hasNext())
                .build();
    }

    public static @NotNull CollectionAction fromCollectionAction(final @NotNull CollectionActionRequestDTO dto) {
        return CollectionAction.builder()
                .type(dto.getType())
                .outcome(dto.getOutcome())
                .notes(dto.getNotes())
                .performedByAgentId(dto.getPerformedByAgentId())
                .performedAt(LocalDateTime.now())
                .build();
    }

    public static CollectionActionResponseDTO fromCollectionAction(final @NotNull CollectionAction action) {
        return CollectionActionResponseDTO.builder()
                .id(action.getId())
                .type(action.getType())
                .outcome(action.getOutcome())
                .notes(action.getNotes())
                .performedByAgentId(action.getPerformedByAgentId())
                .performedAt(action.getPerformedAt())
                .delinquentAccountId(action.getDelinquentAccount() != null ? action.getDelinquentAccount().getId() : null)
                .build();
    }

    public static List<CollectionActionResponseDTO> fromListOfCollectionActions(final @NotNull List<CollectionAction> actions) {
        return actions.stream().map(Mapper::fromCollectionAction).toList();
    }
}
