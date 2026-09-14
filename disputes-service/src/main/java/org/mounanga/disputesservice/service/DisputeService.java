package org.mounanga.disputesservice.service;

import org.mounanga.disputesservice.dto.DisputePageResponseDTO;
import org.mounanga.disputesservice.dto.DisputeRequestDTO;
import org.mounanga.disputesservice.dto.DisputeResolutionRequestDTO;
import org.mounanga.disputesservice.dto.DisputeResponseDTO;

public interface DisputeService {

    DisputeResponseDTO openDispute(DisputeRequestDTO dto);
    DisputeResponseDTO getDisputeById(String id);
    DisputePageResponseDTO getDisputesByAccountId(String accountId, int page, int size);
    DisputeResponseDTO resolveDispute(String id, DisputeResolutionRequestDTO dto);
}
