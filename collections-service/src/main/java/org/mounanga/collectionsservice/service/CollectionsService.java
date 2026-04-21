package org.mounanga.collectionsservice.service;

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

import java.util.List;

public interface CollectionsService {

    DelinquentAccountResponseDTO getDelinquentAccountById(String id);

    DelinquentAccountPageResponseDTO searchDelinquentAccounts(DelinquencyBucket bucket,
                                                              ProductType productType,
                                                              Region region,
                                                              CollectionStatus status,
                                                              int page,
                                                              int size);

    DelinquentAccountResponseDTO createDelinquentAccount(DelinquentAccountRequestDTO dto);

    DelinquentAccountResponseDTO updateStatus(String id, StatusUpdateRequestDTO dto);

    DelinquentAccountResponseDTO assignAgent(String id, AgentAssignmentRequestDTO dto);

    CollectionActionResponseDTO recordAction(String id, CollectionActionRequestDTO dto);

    List<CollectionActionResponseDTO> getActions(String id);

    List<DelinquencySummaryDTO> getDelinquencySummary();
}
