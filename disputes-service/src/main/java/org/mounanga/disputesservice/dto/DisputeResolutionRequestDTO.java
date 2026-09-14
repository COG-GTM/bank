package org.mounanga.disputesservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.mounanga.disputesservice.enums.DisputeStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class DisputeResolutionRequestDTO {
    @NotNull(message = "field 'status' is mandatory: it can not be null")
    private DisputeStatus status;

    private String resolutionNote;
}
