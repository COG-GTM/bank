package org.mounanga.collectionsservice.dto;

import org.junit.jupiter.api.Test;
import org.mounanga.collectionsservice.common.enums.CollectionStatus;
import org.mounanga.collectionsservice.entity.CollectionCase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    @Test
    void testToDTO() {
        LocalDateTime now = LocalDateTime.now();
        CollectionCase entity = CollectionCase.builder()
                .id("case-1")
                .accountId("account-1")
                .overdraftAmount(BigDecimal.valueOf(500))
                .threshold(BigDecimal.valueOf(100))
                .status(CollectionStatus.COLLECTION_INITIATED)
                .flaggedAt(now)
                .build();

        CollectionCaseResponseDTO dto = Mapper.toDTO(entity);

        assertNotNull(dto);
        assertEquals("case-1", dto.getId());
        assertEquals("account-1", dto.getAccountId());
        assertEquals(BigDecimal.valueOf(500), dto.getOverdraftAmount());
        assertEquals(BigDecimal.valueOf(100), dto.getThreshold());
        assertEquals(CollectionStatus.COLLECTION_INITIATED, dto.getStatus());
        assertEquals(now, dto.getFlaggedAt());
        assertNull(dto.getResolvedAt());
        assertNull(dto.getResolvedBy());
    }

    @Test
    void testToDTO_null() {
        assertNull(Mapper.toDTO(null));
    }

    @Test
    void testToDTO_resolvedCase() {
        LocalDateTime flaggedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime resolvedAt = LocalDateTime.now();
        CollectionCase entity = CollectionCase.builder()
                .id("case-2")
                .accountId("account-2")
                .overdraftAmount(BigDecimal.valueOf(1000))
                .threshold(BigDecimal.valueOf(200))
                .status(CollectionStatus.RESOLVED)
                .flaggedAt(flaggedAt)
                .resolvedAt(resolvedAt)
                .resolvedBy("admin")
                .build();

        CollectionCaseResponseDTO dto = Mapper.toDTO(entity);

        assertNotNull(dto);
        assertEquals(CollectionStatus.RESOLVED, dto.getStatus());
        assertEquals(resolvedAt, dto.getResolvedAt());
        assertEquals("admin", dto.getResolvedBy());
    }
}
