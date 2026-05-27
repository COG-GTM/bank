package org.mounanga.collectionsservice.dto;

import lombok.*;
import org.mounanga.collectionsservice.common.enums.CollectionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CollectionCaseResponseDTO {
    private String id;
    private String accountId;
    private BigDecimal overdraftAmount;
    private BigDecimal threshold;
    private CollectionStatus status;
    private LocalDateTime flaggedAt;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
}
