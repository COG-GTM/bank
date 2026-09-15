package org.mounanga.disputesservice.util.mappers;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.mounanga.disputesservice.dto.AccountResponseDTO;
import org.mounanga.disputesservice.dto.DisputePageResponseDTO;
import org.mounanga.disputesservice.dto.DisputeRequestDTO;
import org.mounanga.disputesservice.dto.DisputeResolutionRequestDTO;
import org.mounanga.disputesservice.dto.DisputeResponseDTO;
import org.mounanga.disputesservice.entity.Dispute;
import org.mounanga.disputesservice.enums.DisputeStatus;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Utility class for mapping between DTOs and Entities in the disputes service.
 * <p>
 * This class cannot be instantiated.
 * </p>
 */
public class Mapper {

    private Mapper() {
        super();
    }

    public static @NotNull Dispute fromDispute(final @NotNull DisputeRequestDTO dto, final @NotNull AccountResponseDTO account) {
        final Dispute dispute = new Dispute();
        dispute.setAccountId(account.getId());
        dispute.setCustomerId(account.getCustomerId());
        dispute.setCurrency(account.getCurrency());
        dispute.setAmount(dto.getAmount());
        dispute.setReason(dto.getReason());
        dispute.setDescription(dto.getDescription());
        dispute.setStatus(DisputeStatus.OPEN);
        return dispute;
    }

    public static DisputeResponseDTO fromDispute(final @NotNull Dispute dispute) {
        return DisputeResponseDTO.builder()
                .id(dispute.getId())
                .accountId(dispute.getAccountId())
                .customerId(dispute.getCustomerId())
                .amount(dispute.getAmount())
                .currency(dispute.getCurrency())
                .reason(dispute.getReason())
                .description(dispute.getDescription())
                .status(dispute.getStatus())
                .resolutionNote(dispute.getResolutionNote())
                .createdDate(dispute.getCreatedDate())
                .createdBy(dispute.getCreatedBy())
                .lastModifiedDate(dispute.getLastModifiedDate())
                .lastModifiedBy(dispute.getLastModifiedBy())
                .build();
    }

    public static List<DisputeResponseDTO> fromListOfDisputes(final @NotNull List<Dispute> disputes) {
        return disputes.stream().map(Mapper::fromDispute).toList();
    }

    public static DisputePageResponseDTO fromPageOfDisputes(final @NotNull Page<Dispute> pageOfDisputes) {
        return DisputePageResponseDTO.builder()
                .disputes(fromListOfDisputes(pageOfDisputes.getContent()))
                .totalPages(pageOfDisputes.getTotalPages())
                .totalElements(pageOfDisputes.getTotalElements())
                .size(pageOfDisputes.getSize())
                .numberOfElements(pageOfDisputes.getNumberOfElements())
                .number(pageOfDisputes.getNumber())
                .hasContent(pageOfDisputes.hasContent())
                .isFirst(pageOfDisputes.isFirst())
                .isLast(pageOfDisputes.isLast())
                .hasPrevious(pageOfDisputes.hasPrevious())
                .hasNext(pageOfDisputes.hasNext())
                .build();
    }

    @Contract("_, _ -> param1")
    public static @NotNull Dispute updateDisputeItems(@NotNull Dispute dispute, final @NotNull DisputeResolutionRequestDTO dto) {
        dispute.setStatus(dto.getStatus());
        dispute.setResolutionNote(dto.getResolutionNote());
        return dispute;
    }
}
