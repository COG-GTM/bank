package org.mounanga.disputesservice.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.mounanga.disputesservice.dto.DisputePageResponseDTO;
import org.mounanga.disputesservice.dto.DisputeRequestDTO;
import org.mounanga.disputesservice.dto.DisputeResolutionRequestDTO;
import org.mounanga.disputesservice.dto.DisputeResponseDTO;
import org.mounanga.disputesservice.service.DisputeService;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
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
                                                        @RequestParam(defaultValue = "0") @Min(value = 0, message = "parameter 'page' must be greater than or equal to 0") int page,
                                                        @RequestParam(defaultValue = "9") @Min(value = 1, message = "parameter 'size' must be greater than 0") int size) {
        return disputeService.getDisputesByAccountId(accountId, page, size);
    }

    @PatchMapping("/resolve/{id}")
    public DisputeResponseDTO resolveDispute(@PathVariable String id, @RequestBody @Valid DisputeResolutionRequestDTO dto) {
        return disputeService.resolveDispute(id, dto);
    }
}
