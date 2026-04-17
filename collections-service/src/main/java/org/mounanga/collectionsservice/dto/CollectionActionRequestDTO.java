package org.mounanga.collectionsservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CollectionActionRequestDTO {

    @NotBlank(message = "Action type is required")
    private String actionType;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Action date is required")
    private LocalDateTime actionDate;

    private String outcome;

    private LocalDateTime followUpDate;
}
