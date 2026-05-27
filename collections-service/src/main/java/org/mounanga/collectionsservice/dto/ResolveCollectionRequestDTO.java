package org.mounanga.collectionsservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResolveCollectionRequestDTO {
    @NotBlank(message = "resolvedBy is required")
    private String resolvedBy;
}
