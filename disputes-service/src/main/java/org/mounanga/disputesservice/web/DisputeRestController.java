package org.mounanga.disputesservice.web;

import jakarta.validation.Valid;
import org.mounanga.disputesservice.dto.DisputePageResponseDTO;
import org.mounanga.disputesservice.dto.DisputeRequestDTO;
import org.mounanga.disputesservice.dto.DisputeResolutionRequestDTO;
import org.mounanga.disputesservice.dto.DisputeResponseDTO;
import org.mounanga.disputesservice.service.DisputeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/disputes")
public class DisputeRestController {

    private final DisputeService disputeService;

    public DisputeRestController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    @PostMapping("/open")
    public DisputeResponseDTO openDispute(@RequestBody @Valid DisputeRequestDTO dto) {
        return disputeService.openDispute(dto);
    }

    @GetMapping("/get/{id}")
    public DisputeResponseDTO getDisputeById(@PathVariable String id) {
        return disputeService.getDisputeById(id);
    }

    @GetMapping("/list")
    public DisputePageResponseDTO getDisputesByAccountId(@RequestParam(name = "accountId") String accountId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "9") int size) {
        return disputeService.getDisputesByAccountId(accountId, page, size);
    }

    @PatchMapping("/resolve/{id}")
    public DisputeResponseDTO resolveDispute(@PathVariable String id, @RequestBody @Valid DisputeResolutionRequestDTO dto) {
        return disputeService.resolveDispute(id, dto);
    }
}
