package org.mounanga.collectionsservice.dto;

import org.mounanga.collectionsservice.entity.CollectionCase;

public class Mapper {

    private Mapper() {
    }

    public static CollectionCaseResponseDTO toDTO(CollectionCase entity) {
        if (entity == null) {
            return null;
        }
        return CollectionCaseResponseDTO.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .overdraftAmount(entity.getOverdraftAmount())
                .threshold(entity.getThreshold())
                .status(entity.getStatus())
                .flaggedAt(entity.getFlaggedAt())
                .resolvedAt(entity.getResolvedAt())
                .resolvedBy(entity.getResolvedBy())
                .build();
    }
}
