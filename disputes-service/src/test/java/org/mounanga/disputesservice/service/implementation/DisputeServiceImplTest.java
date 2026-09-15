package org.mounanga.disputesservice.service.implementation;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mounanga.disputesservice.dto.AccountResponseDTO;
import org.mounanga.disputesservice.dto.DisputePageResponseDTO;
import org.mounanga.disputesservice.dto.DisputeRequestDTO;
import org.mounanga.disputesservice.dto.DisputeResolutionRequestDTO;
import org.mounanga.disputesservice.dto.DisputeResponseDTO;
import org.mounanga.disputesservice.entity.Dispute;
import org.mounanga.disputesservice.enums.Currency;
import org.mounanga.disputesservice.enums.DisputeReason;
import org.mounanga.disputesservice.enums.DisputeStatus;
import org.mounanga.disputesservice.exception.AccountNotFoundException;
import org.mounanga.disputesservice.exception.AccountServiceUnavailableException;
import org.mounanga.disputesservice.exception.DisputeNotFoundException;
import org.mounanga.disputesservice.exception.FieldValidationException;
import org.mounanga.disputesservice.repository.DisputeRepository;
import org.mounanga.disputesservice.web.AccountRestClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeServiceImplTest {

    private DisputeServiceImpl disputeService;

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private AccountRestClient accountRestClient;

    @BeforeEach
    void setUp() {
        disputeService = new DisputeServiceImpl(disputeRepository, accountRestClient);
    }

    @Test
    void testOpenDispute() {
        DisputeRequestDTO dto = DisputeRequestDTO.builder()
                .accountId("ACC-1")
                .amount(new BigDecimal("49.99"))
                .reason(DisputeReason.DUPLICATE_CHARGE)
                .description("charged twice")
                .build();

        AccountResponseDTO account = AccountResponseDTO.builder()
                .id("ACC-1").customerId("CUST-1").currency(Currency.EUR).build();

        Dispute saved = new Dispute();
        saved.setId("DSP-1");
        saved.setAccountId("ACC-1");
        saved.setCustomerId("CUST-1");
        saved.setCurrency(Currency.EUR);
        saved.setAmount(dto.getAmount());
        saved.setReason(dto.getReason());
        saved.setStatus(DisputeStatus.OPEN);

        when(accountRestClient.getAccountById("ACC-1")).thenReturn(account);
        when(disputeRepository.save(any(Dispute.class))).thenReturn(saved);

        DisputeResponseDTO result = disputeService.openDispute(dto);

        assertNotNull(result);
        assertEquals("DSP-1", result.getId());
        assertEquals("CUST-1", result.getCustomerId());
        assertEquals(Currency.EUR, result.getCurrency());
        assertEquals(DisputeStatus.OPEN, result.getStatus());

        verify(accountRestClient, times(1)).getAccountById("ACC-1");
        verify(disputeRepository, times(1)).save(any(Dispute.class));
    }

    @Test
    void testOpenDisputeAccountNotFound() {
        DisputeRequestDTO dto = DisputeRequestDTO.builder()
                .accountId("UNKNOWN")
                .amount(BigDecimal.TEN)
                .reason(DisputeReason.OTHER)
                .build();

        when(accountRestClient.getAccountById("UNKNOWN")).thenThrow(feignException(404));

        assertThrows(AccountNotFoundException.class, () -> disputeService.openDispute(dto));

        verify(accountRestClient, times(1)).getAccountById("UNKNOWN");
        verify(disputeRepository, never()).save(any(Dispute.class));
    }

    @Test
    void testOpenDisputeAccountServiceUnavailable() {
        DisputeRequestDTO dto = DisputeRequestDTO.builder()
                .accountId("ACC-1")
                .amount(BigDecimal.TEN)
                .reason(DisputeReason.OTHER)
                .build();

        when(accountRestClient.getAccountById("ACC-1")).thenThrow(feignException(503));

        assertThrows(AccountServiceUnavailableException.class, () -> disputeService.openDispute(dto));

        verify(disputeRepository, never()).save(any(Dispute.class));
    }

    private static FeignException feignException(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "/bank/accounts/queries/get-account/x",
                Map.of(), null, null, null);
        return FeignException.errorStatus("getAccountById",
                feign.Response.builder().status(status).request(request).headers(Map.of()).build());
    }

    @Test
    void testGetDisputeById() {
        Dispute dispute = new Dispute();
        dispute.setId("DSP-1");
        when(disputeRepository.findById(anyString())).thenReturn(Optional.of(dispute));

        DisputeResponseDTO result = disputeService.getDisputeById("DSP-1");

        assertNotNull(result);
        assertEquals("DSP-1", result.getId());
        verify(disputeRepository, times(1)).findById("DSP-1");
    }

    @Test
    void testGetDisputeByIdNotFound() {
        when(disputeRepository.findById(anyString())).thenReturn(Optional.empty());
        assertThrows(DisputeNotFoundException.class, () -> disputeService.getDisputeById("DSP-1"));
        verify(disputeRepository, times(1)).findById("DSP-1");
    }

    @Test
    void testGetDisputesByAccountId() {
        Pageable pageable = PageRequest.of(0, 10);
        Dispute dispute = new Dispute();
        dispute.setAccountId("ACC-1");
        Page<Dispute> page = new PageImpl<>(List.of(dispute), pageable, 1);
        when(disputeRepository.findByAccountIdOrderByCreatedDateDesc("ACC-1", pageable)).thenReturn(page);

        DisputePageResponseDTO result = disputeService.getDisputesByAccountId("ACC-1", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("ACC-1", result.getDisputes().getFirst().getAccountId());
        verify(disputeRepository, times(1)).findByAccountIdOrderByCreatedDateDesc("ACC-1", pageable);
    }

    @Test
    void testResolveDispute() {
        Dispute existing = new Dispute();
        existing.setId("DSP-1");
        existing.setStatus(DisputeStatus.UNDER_REVIEW);

        DisputeResolutionRequestDTO dto = DisputeResolutionRequestDTO.builder()
                .status(DisputeStatus.RESOLVED_CUSTOMER)
                .resolutionNote("refunded")
                .build();

        when(disputeRepository.findById("DSP-1")).thenReturn(Optional.of(existing));
        when(disputeRepository.save(existing)).thenReturn(existing);

        DisputeResponseDTO result = disputeService.resolveDispute("DSP-1", dto);

        assertEquals(DisputeStatus.RESOLVED_CUSTOMER, result.getStatus());
        assertEquals("refunded", result.getResolutionNote());
        verify(disputeRepository, times(1)).save(existing);
    }

    @Test
    void testResolveDisputeAlreadyClosed() {
        Dispute existing = new Dispute();
        existing.setId("DSP-1");
        existing.setStatus(DisputeStatus.REJECTED);
        DisputeResolutionRequestDTO dto = DisputeResolutionRequestDTO.builder()
                .status(DisputeStatus.RESOLVED_MERCHANT).build();

        when(disputeRepository.findById("DSP-1")).thenReturn(Optional.of(existing));

        assertThrows(FieldValidationException.class, () -> disputeService.resolveDispute("DSP-1", dto));
        verify(disputeRepository, never()).save(any(Dispute.class));
    }

    @Test
    void testResolveDisputeBackToOpenRejected() {
        Dispute existing = new Dispute();
        existing.setId("DSP-1");
        existing.setStatus(DisputeStatus.UNDER_REVIEW);
        DisputeResolutionRequestDTO dto = DisputeResolutionRequestDTO.builder()
                .status(DisputeStatus.OPEN).build();

        when(disputeRepository.findById("DSP-1")).thenReturn(Optional.of(existing));

        assertThrows(FieldValidationException.class, () -> disputeService.resolveDispute("DSP-1", dto));
        verify(disputeRepository, never()).save(any(Dispute.class));
    }

    @Test
    void testResolveDisputeNotFound() {
        DisputeResolutionRequestDTO dto = DisputeResolutionRequestDTO.builder()
                .status(DisputeStatus.REJECTED).build();
        when(disputeRepository.findById("DSP-1")).thenReturn(Optional.empty());
        assertThrows(DisputeNotFoundException.class, () -> disputeService.resolveDispute("DSP-1", dto));
    }
}
