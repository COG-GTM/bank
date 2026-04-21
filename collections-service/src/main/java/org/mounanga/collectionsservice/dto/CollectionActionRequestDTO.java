package org.mounanga.collectionsservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.mounanga.collectionsservice.enums.ActionOutcome;
import org.mounanga.collectionsservice.enums.ActionType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CollectionActionRequestDTO {

    @NotNull(message = "field 'type' is mandatory: it can not be null")
    private ActionType type;

    @NotNull(message = "field 'outcome' is mandatory: it can not be null")
    private ActionOutcome outcome;

    @NotBlank(message = "field 'notes' is mandatory: it can not be blank")
    private String notes;

    @NotBlank(message = "field 'performedByAgentId' is mandatory: it can not be blank")
    private String performedByAgentId;
}
