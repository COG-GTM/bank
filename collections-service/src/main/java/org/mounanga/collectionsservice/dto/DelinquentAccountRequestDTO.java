package org.mounanga.collectionsservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.mounanga.collectionsservice.enums.DelinquencyBucket;
import org.mounanga.collectionsservice.enums.ProductType;
import org.mounanga.collectionsservice.enums.Region;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class DelinquentAccountRequestDTO {

    @NotBlank(message = "field 'accountNumber' is mandatory: it can not be blank")
    private String accountNumber;

    @NotBlank(message = "field 'customerId' is mandatory: it can not be blank")
    private String customerId;

    @NotBlank(message = "field 'accountHolderName' is mandatory: it can not be blank")
    private String accountHolderName;

    @NotBlank(message = "field 'accountHolderEmail' is mandatory: it can not be blank")
    @Email(message = "field 'accountHolderEmail' is not well formated")
    private String accountHolderEmail;

    private String accountHolderPhone;

    @NotNull(message = "field 'productType' is mandatory: it can not be null")
    private ProductType productType;

    @NotNull(message = "field 'region' is mandatory: it can not be null")
    private Region region;

    @NotNull(message = "field 'delinquencyBucket' is mandatory: it can not be null")
    private DelinquencyBucket delinquencyBucket;

    @NotNull(message = "field 'outstandingBalance' is mandatory: it can not be null")
    @PositiveOrZero(message = "field 'outstandingBalance' must be zero or positive")
    private BigDecimal outstandingBalance;

    @NotNull(message = "field 'daysPastDue' is mandatory: it can not be null")
    @PositiveOrZero(message = "field 'daysPastDue' must be zero or positive")
    private Integer daysPastDue;

    @NotNull(message = "field 'lastPaymentDate' is mandatory: it can not be null")
    private LocalDate lastPaymentDate;
}
