package org.mounanga.disputesservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.mounanga.disputesservice.enums.DisputeReason;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class DisputeRequestDTO {
    @NotBlank(message = "field 'accountId' is mandatory: it can not be blank")
    private String accountId;

    @NotNull(message = "field 'amount' is mandatory: it can not be null")
    @DecimalMin(value = "0.01", message = "field 'amount' must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "field 'reason' is mandatory: it can not be null")
    private DisputeReason reason;

    @Size(max = 1024, message = "field 'description' must not exceed 1024 characters")
    private String description;
}
