package org.mounanga.collectionsservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class AgentAssignmentRequestDTO {

    @NotBlank(message = "field 'agentId' is mandatory: it can not be blank")
    private String agentId;
}
