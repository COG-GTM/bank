package org.mounanga.collectionsservice.web;

import jakarta.validation.Valid;
import org.mounanga.collectionsservice.dto.*;
import org.mounanga.collectionsservice.enums.CollectionStatus;
import org.mounanga.collectionsservice.enums.DelinquencyBucket;
import org.mounanga.collectionsservice.enums.ProductType;
import org.mounanga.collectionsservice.enums.Region;
import org.mounanga.collectionsservice.service.CollectionsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collections")
public class CollectionsRestController {

    private final CollectionsService collectionsService;

    public CollectionsRestController(CollectionsService collectionsService) {
        this.collectionsService = collectionsService;
    }

    @GetMapping("/delinquent-accounts")
    public DelinquentAccountPageResponseDTO getDelinquentAccounts(
            @RequestParam(required = false) DelinquencyBucket bucket,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) Region region,
            @RequestParam(required = false) CollectionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return collectionsService.getDelinquentAccounts(bucket, productType, region, status, page, size);
    }

    @GetMapping("/delinquent-accounts/{id}")
    public DelinquentAccountResponseDTO getDelinquentAccountById(@PathVariable String id) {
        return collectionsService.getDelinquentAccountById(id);
    }

    @GetMapping("/delinquent-accounts/by-account/{accountId}")
    public DelinquentAccountResponseDTO getDelinquentAccountByAccountId(@PathVariable String accountId) {
        return collectionsService.getDelinquentAccountByAccountId(accountId);
    }

    @GetMapping("/delinquent-accounts/by-customer/{customerId}")
    public List<DelinquentAccountResponseDTO> getDelinquentAccountsByCustomerId(@PathVariable String customerId) {
        return collectionsService.getDelinquentAccountsByCustomerId(customerId);
    }

    @GetMapping("/delinquent-accounts/search")
    public DelinquentAccountPageResponseDTO searchDelinquentAccounts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return collectionsService.searchDelinquentAccounts(keyword, page, size);
    }

    @GetMapping("/summary")
    public List<DelinquencySummaryDTO> getDelinquencySummary() {
        return collectionsService.getDelinquencySummary();
    }

    @PutMapping("/delinquent-accounts/{id}/status")
    public DelinquentAccountResponseDTO updateCollectionStatus(
            @PathVariable String id,
            @RequestParam CollectionStatus status) {
        return collectionsService.updateCollectionStatus(id, status);
    }

    @PutMapping("/delinquent-accounts/{id}/assign")
    public DelinquentAccountResponseDTO assignAgent(
            @PathVariable String id,
            @RequestParam String agentId) {
        return collectionsService.assignAgent(id, agentId);
    }

    @PostMapping("/delinquent-accounts/{id}/actions")
    @ResponseStatus(HttpStatus.CREATED)
    public CollectionActionResponseDTO addCollectionAction(
            @PathVariable String id,
            @RequestBody @Valid CollectionActionRequestDTO request) {
        return collectionsService.addCollectionAction(id, request);
    }

    @GetMapping("/delinquent-accounts/{id}/actions")
    public List<CollectionActionResponseDTO> getCollectionActions(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return collectionsService.getCollectionActions(id, page, size);
    }
}
