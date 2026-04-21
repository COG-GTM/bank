package org.mounanga.collectionsservice.web;

import jakarta.validation.Valid;
import org.mounanga.collectionsservice.dto.AgentAssignmentRequestDTO;
import org.mounanga.collectionsservice.dto.CollectionActionRequestDTO;
import org.mounanga.collectionsservice.dto.CollectionActionResponseDTO;
import org.mounanga.collectionsservice.dto.DelinquencySummaryDTO;
import org.mounanga.collectionsservice.dto.DelinquentAccountPageResponseDTO;
import org.mounanga.collectionsservice.dto.DelinquentAccountRequestDTO;
import org.mounanga.collectionsservice.dto.DelinquentAccountResponseDTO;
import org.mounanga.collectionsservice.dto.StatusUpdateRequestDTO;
import org.mounanga.collectionsservice.enums.CollectionStatus;
import org.mounanga.collectionsservice.enums.DelinquencyBucket;
import org.mounanga.collectionsservice.enums.ProductType;
import org.mounanga.collectionsservice.enums.Region;
import org.mounanga.collectionsservice.service.CollectionsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collections")
public class CollectionsRestController {

    private final CollectionsService collectionsService;

    public CollectionsRestController(CollectionsService collectionsService) {
        this.collectionsService = collectionsService;
    }

    @GetMapping("/delinquent-accounts/{id}")
    public DelinquentAccountResponseDTO getById(@PathVariable String id) {
        return collectionsService.getDelinquentAccountById(id);
    }

    @GetMapping("/delinquent-accounts")
    public DelinquentAccountPageResponseDTO search(@RequestParam(required = false) DelinquencyBucket bucket,
                                                   @RequestParam(required = false) ProductType productType,
                                                   @RequestParam(required = false) Region region,
                                                   @RequestParam(required = false) CollectionStatus status,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return collectionsService.searchDelinquentAccounts(bucket, productType, region, status, page, size);
    }

    @PostMapping("/delinquent-accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public DelinquentAccountResponseDTO create(@RequestBody @Valid DelinquentAccountRequestDTO dto) {
        return collectionsService.createDelinquentAccount(dto);
    }

    @PutMapping("/delinquent-accounts/{id}/status")
    public DelinquentAccountResponseDTO updateStatus(@PathVariable String id,
                                                     @RequestBody @Valid StatusUpdateRequestDTO dto) {
        return collectionsService.updateStatus(id, dto);
    }

    @PutMapping("/delinquent-accounts/{id}/assign")
    public DelinquentAccountResponseDTO assignAgent(@PathVariable String id,
                                                    @RequestBody @Valid AgentAssignmentRequestDTO dto) {
        return collectionsService.assignAgent(id, dto);
    }

    @PostMapping("/delinquent-accounts/{id}/actions")
    @ResponseStatus(HttpStatus.CREATED)
    public CollectionActionResponseDTO recordAction(@PathVariable String id,
                                                    @RequestBody @Valid CollectionActionRequestDTO dto) {
        return collectionsService.recordAction(id, dto);
    }

    @GetMapping("/delinquent-accounts/{id}/actions")
    public ResponseEntity<List<CollectionActionResponseDTO>> getActions(@PathVariable String id) {
        return ResponseEntity.ok(collectionsService.getActions(id));
    }

    @GetMapping("/summary")
    public List<DelinquencySummaryDTO> getSummary() {
        return collectionsService.getDelinquencySummary();
    }
}
