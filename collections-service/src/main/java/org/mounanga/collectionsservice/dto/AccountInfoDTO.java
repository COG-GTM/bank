package org.mounanga.collectionsservice.dto;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class AccountInfoDTO {
    private String id;
    private String status;
    private BigDecimal balance;
    private String currency;
    private String customerId;
}
