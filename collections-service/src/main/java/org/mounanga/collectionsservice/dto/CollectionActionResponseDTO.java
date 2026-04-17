package org.mounanga.collectionsservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CollectionActionResponseDTO {
    private String id;
    private String actionType;
    private String description;
    private String performedBy;
    private LocalDateTime actionDate;
    private String outcome;
    private LocalDateTime followUpDate;
    private String delinquentAccountId;
    private LocalDateTime createdDate;
    private String createdBy;
}
