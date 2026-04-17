package org.mounanga.collectionsservice.service;

import org.mounanga.collectionsservice.dto.*;
import org.mounanga.collectionsservice.enums.CollectionStatus;
import org.mounanga.collectionsservice.enums.DelinquencyBucket;
import org.mounanga.collectionsservice.enums.ProductType;
import org.mounanga.collectionsservice.enums.Region;

import java.util.List;

public interface CollectionsService {

    DelinquentAccountPageResponseDTO getDelinquentAccounts(
            DelinquencyBucket bucket,
            ProductType productType,
            Region region,
            CollectionStatus status,
            int page,
            int size);

    DelinquentAccountResponseDTO getDelinquentAccountById(String id);

    DelinquentAccountResponseDTO getDelinquentAccountByAccountId(String accountId);

    List<DelinquentAccountResponseDTO> getDelinquentAccountsByCustomerId(String customerId);

    DelinquentAccountPageResponseDTO searchDelinquentAccounts(String keyword, int page, int size);

    List<DelinquencySummaryDTO> getDelinquencySummary();

    DelinquentAccountResponseDTO updateCollectionStatus(String id, CollectionStatus status);

    DelinquentAccountResponseDTO assignAgent(String id, String agentId);

    CollectionActionResponseDTO addCollectionAction(String delinquentAccountId, CollectionActionRequestDTO request);

    List<CollectionActionResponseDTO> getCollectionActions(String delinquentAccountId, int page, int size);
}
