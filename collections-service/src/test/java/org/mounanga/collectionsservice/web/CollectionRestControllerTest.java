package org.mounanga.collectionsservice.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mounanga.collectionsservice.common.enums.CollectionStatus;
import org.mounanga.collectionsservice.dto.CollectionCaseResponseDTO;
import org.mounanga.collectionsservice.dto.ResolveCollectionRequestDTO;
import org.mounanga.collectionsservice.entity.CollectionCase;
import org.mounanga.collectionsservice.repository.CollectionCaseRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "eureka.client.enabled=false",
        "axon.axonserver.enabled=false",
        "collections.overdraft-threshold=0"
})
class CollectionRestControllerTest {

    @Mock
    private CollectionCaseRepository collectionCaseRepository;

    @InjectMocks
    private CollectionRestController collectionRestController;

    private CollectionCase collectionCase;

    @BeforeEach
    void setUp() {
        collectionRestController = new CollectionRestController(collectionCaseRepository);
        collectionCase = CollectionCase.builder()
                .id("case-1")
                .accountId("account-1")
                .overdraftAmount(BigDecimal.valueOf(500))
                .threshold(BigDecimal.valueOf(100))
                .status(CollectionStatus.COLLECTION_INITIATED)
                .flaggedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetAllCollections() {
        when(collectionCaseRepository.findAll()).thenReturn(List.of(collectionCase));

        ResponseEntity<List<CollectionCaseResponseDTO>> response = collectionRestController.getAllCollections();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("case-1", response.getBody().get(0).getId());
    }

    @Test
    void testGetCollectionById_found() {
        when(collectionCaseRepository.findById("case-1")).thenReturn(Optional.of(collectionCase));

        ResponseEntity<CollectionCaseResponseDTO> response = collectionRestController.getCollectionById("case-1");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("case-1", response.getBody().getId());
        assertEquals("account-1", response.getBody().getAccountId());
    }

    @Test
    void testGetCollectionById_notFound() {
        when(collectionCaseRepository.findById("unknown")).thenReturn(Optional.empty());

        ResponseEntity<CollectionCaseResponseDTO> response = collectionRestController.getCollectionById("unknown");

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetCollectionsByAccountId() {
        when(collectionCaseRepository.findByAccountId("account-1")).thenReturn(List.of(collectionCase));

        ResponseEntity<List<CollectionCaseResponseDTO>> response = collectionRestController.getCollectionsByAccountId("account-1");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetCollectionsByStatus() {
        when(collectionCaseRepository.findByStatus(CollectionStatus.COLLECTION_INITIATED)).thenReturn(List.of(collectionCase));

        ResponseEntity<List<CollectionCaseResponseDTO>> response = collectionRestController.getCollectionsByStatus(CollectionStatus.COLLECTION_INITIATED);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testResolveCollection_found() {
        when(collectionCaseRepository.findById("case-1")).thenReturn(Optional.of(collectionCase));
        CollectionCase resolved = CollectionCase.builder()
                .id("case-1")
                .accountId("account-1")
                .overdraftAmount(BigDecimal.valueOf(500))
                .threshold(BigDecimal.valueOf(100))
                .status(CollectionStatus.RESOLVED)
                .flaggedAt(collectionCase.getFlaggedAt())
                .resolvedAt(LocalDateTime.now())
                .resolvedBy("admin")
                .build();
        when(collectionCaseRepository.save(any(CollectionCase.class))).thenReturn(resolved);

        ResolveCollectionRequestDTO request = new ResolveCollectionRequestDTO("admin");
        ResponseEntity<CollectionCaseResponseDTO> response = collectionRestController.resolveCollection("case-1", request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(CollectionStatus.RESOLVED, response.getBody().getStatus());
        assertEquals("admin", response.getBody().getResolvedBy());
    }

    @Test
    void testResolveCollection_notFound() {
        when(collectionCaseRepository.findById("unknown")).thenReturn(Optional.empty());

        ResolveCollectionRequestDTO request = new ResolveCollectionRequestDTO("admin");
        ResponseEntity<CollectionCaseResponseDTO> response = collectionRestController.resolveCollection("unknown", request);

        assertEquals(404, response.getStatusCode().value());
    }
}
