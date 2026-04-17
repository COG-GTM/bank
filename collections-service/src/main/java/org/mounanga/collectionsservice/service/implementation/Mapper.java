package org.mounanga.collectionsservice.service.implementation;

import org.jetbrains.annotations.NotNull;
import org.mounanga.collectionsservice.dto.*;
import org.mounanga.collectionsservice.entity.CollectionAction;
import org.mounanga.collectionsservice.entity.DelinquentAccount;
import org.springframework.data.domain.Page;

import java.util.List;

public class Mapper {

    private Mapper() {
        super();
    }

    @NotNull
    public static DelinquentAccountResponseDTO fromDelinquentAccount(@NotNull final DelinquentAccount account) {
        return DelinquentAccountResponseDTO.builder()
                .id(account.getId())
                .accountId(account.getAccountId())
                .customerId(account.getCustomerId())
                .customerName(account.getCustomerName())
                .email(account.getEmail())
                .outstandingBalance(account.getOutstandingBalance())
                .minimumPaymentDue(account.getMinimumPaymentDue())
                .lastPaymentDate(account.getLastPaymentDate())
                .dueDate(account.getDueDate())
                .daysOverdue(account.getDaysOverdue())
                .delinquencyBucket(account.getDelinquencyBucket())
                .productType(account.getProductType())
                .region(account.getRegion())
                .collectionStatus(account.getCollectionStatus())
                .assignedAgentId(account.getAssignedAgentId())
                .createdDate(account.getCreatedDate())
                .createdBy(account.getCreatedBy())
                .lastModifiedDate(account.getLastModifiedDate())
                .lastModifiedBy(account.getLastModifiedBy())
                .build();
    }

    public static List<DelinquentAccountResponseDTO> fromDelinquentAccounts(@NotNull final List<DelinquentAccount> accounts) {
        return accounts.stream().map(Mapper::fromDelinquentAccount).toList();
    }

    @NotNull
    public static DelinquentAccountPageResponseDTO fromPageOfDelinquentAccounts(@NotNull final Page<DelinquentAccount> page) {
        return DelinquentAccountPageResponseDTO.builder()
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .numberOfElements(page.getNumberOfElements())
                .number(page.getNumber())
                .hasContent(page.hasContent())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .delinquentAccounts(fromDelinquentAccounts(page.getContent()))
                .build();
    }

    @NotNull
    public static CollectionActionResponseDTO fromCollectionAction(@NotNull final CollectionAction action) {
        return CollectionActionResponseDTO.builder()
                .id(action.getId())
                .actionType(action.getActionType())
                .description(action.getDescription())
                .performedBy(action.getPerformedBy())
                .actionDate(action.getActionDate())
                .outcome(action.getOutcome())
                .followUpDate(action.getFollowUpDate())
                .delinquentAccountId(action.getDelinquentAccount() != null ? action.getDelinquentAccount().getId() : null)
                .createdDate(action.getCreatedDate())
                .createdBy(action.getCreatedBy())
                .build();
    }

    public static List<CollectionActionResponseDTO> fromCollectionActions(@NotNull final List<CollectionAction> actions) {
        return actions.stream().map(Mapper::fromCollectionAction).toList();
    }
}
