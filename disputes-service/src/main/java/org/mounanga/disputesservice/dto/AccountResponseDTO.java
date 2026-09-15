package org.mounanga.disputesservice.dto;

import lombok.*;
import org.mounanga.disputesservice.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class AccountResponseDTO {
    private String id;
    private String status;
    private BigDecimal balance;
    private Currency currency;
    private String customerId;
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime lastModifiedDate;
    private String lastModifiedBy;
}
