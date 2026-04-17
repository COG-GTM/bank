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
    private String accountId;
    private String customerId;
    private String customerName;
    private String email;
    private BigDecimal outstandingBalance;
    private BigDecimal minimumPaymentDue;
    private LocalDate lastPaymentDate;
    private LocalDate dueDate;
    private int daysOverdue;
    private DelinquencyBucket delinquencyBucket;
    private ProductType productType;
    private Region region;
    private CollectionStatus collectionStatus;
    private String assignedAgentId;
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime lastModifiedDate;
    private String lastModifiedBy;
}
