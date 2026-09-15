package org.mounanga.disputesservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(max = 1024, message = "field 'resolutionNote' must not exceed 1024 characters")
    private String resolutionNote;
}
