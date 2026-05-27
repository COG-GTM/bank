package org.mounanga.collectionsservice.web;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.mounanga.collectionsservice.common.enums.CollectionStatus;
import org.mounanga.collectionsservice.dto.CollectionCaseResponseDTO;
import org.mounanga.collectionsservice.dto.Mapper;
import org.mounanga.collectionsservice.dto.ResolveCollectionRequestDTO;
import org.mounanga.collectionsservice.entity.CollectionCase;
import org.mounanga.collectionsservice.repository.CollectionCaseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/collections")
@Slf4j
public class CollectionRestController {

    private final CollectionCaseRepository collectionCaseRepository;

    public CollectionRestController(CollectionCaseRepository collectionCaseRepository) {
        this.collectionCaseRepository = collectionCaseRepository;
    }

    @GetMapping
    public ResponseEntity<List<CollectionCaseResponseDTO>> getAllCollections() {
        log.info("GET /collections");
        List<CollectionCaseResponseDTO> cases = collectionCaseRepository.findAll()
                .stream()
                .map(Mapper::toDTO)
                .toList();
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionCaseResponseDTO> getCollectionById(@PathVariable String id) {
        log.info("GET /collections/{}", id);
        return collectionCaseRepository.findById(id)
                .map(Mapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<CollectionCaseResponseDTO>> getCollectionsByAccountId(@PathVariable String accountId) {
        log.info("GET /collections/account/{}", accountId);
        List<CollectionCaseResponseDTO> cases = collectionCaseRepository.findByAccountId(accountId)
                .stream()
                .map(Mapper::toDTO)
                .toList();
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CollectionCaseResponseDTO>> getCollectionsByStatus(@PathVariable CollectionStatus status) {
        log.info("GET /collections/status/{}", status);
        List<CollectionCaseResponseDTO> cases = collectionCaseRepository.findByStatus(status)
                .stream()
                .map(Mapper::toDTO)
                .toList();
        return ResponseEntity.ok(cases);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<CollectionCaseResponseDTO> resolveCollection(
            @PathVariable String id,
            @Valid @RequestBody ResolveCollectionRequestDTO request) {
        log.info("PUT /collections/{}/resolve by {}", id, request.getResolvedBy());
        return collectionCaseRepository.findById(id)
                .map(collectionCase -> {
                    collectionCase.setStatus(CollectionStatus.RESOLVED);
                    collectionCase.setResolvedAt(LocalDateTime.now());
                    collectionCase.setResolvedBy(request.getResolvedBy());
                    CollectionCase saved = collectionCaseRepository.save(collectionCase);
                    return ResponseEntity.ok(Mapper.toDTO(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
