package org.mounanga.collectionsservice.dto;

import lombok.*;
import org.mounanga.collectionsservice.enums.CollectionStatus;
import org.mounanga.collectionsservice.enums.DelinquencyBucket;
import org.mounanga.collectionsservice.enums.ProductType;
import org.mounanga.collectionsservice.enums.Region;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class DelinquentAccountResponseDTO {
    private String id;
    private String accountNumber;
    private String customerId;
    private String accountHolderName;
    private String accountHolderEmail;
    private String accountHolderPhone;
    private ProductType productType;
    private Region region;
    private DelinquencyBucket delinquencyBucket;
    private CollectionStatus status;
    private BigDecimal outstandingBalance;
    private Integer daysPastDue;
    private LocalDate lastPaymentDate;
    private String assignedAgentId;
    private LocalDateTime assignedAt;
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime lastModifiedDate;
    private String lastModifiedBy;
}
