package org.mounanga.collectionsservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.mounanga.collectionsservice.enums.CollectionStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class StatusUpdateRequestDTO {

    @NotNull(message = "field 'status' is mandatory: it can not be null")
    private CollectionStatus status;

    private String reason;
}
