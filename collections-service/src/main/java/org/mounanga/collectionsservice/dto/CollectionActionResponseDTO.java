package org.mounanga.collectionsservice.dto;

import lombok.*;
import org.mounanga.collectionsservice.enums.ActionOutcome;
import org.mounanga.collectionsservice.enums.ActionType;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CollectionActionResponseDTO {
    private String id;
    private ActionType type;
    private ActionOutcome outcome;
    private String notes;
    private String performedByAgentId;
    private LocalDateTime performedAt;
    private String delinquentAccountId;
}
