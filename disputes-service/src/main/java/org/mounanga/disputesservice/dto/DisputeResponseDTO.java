package org.mounanga.disputesservice.dto;

import lombok.*;
import org.mounanga.disputesservice.enums.Currency;
import org.mounanga.disputesservice.enums.DisputeReason;
import org.mounanga.disputesservice.enums.DisputeStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class DisputeResponseDTO {
    private String id;
    private String accountId;
    private String customerId;
    private BigDecimal amount;
    private Currency currency;
    private DisputeReason reason;
    private String description;
    private DisputeStatus status;
    private String resolutionNote;
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime lastModifiedDate;
    private String lastModifiedBy;
}
