package org.mounanga.disputesservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mounanga.disputesservice.configuration.ApplicationProperties;
import org.mounanga.disputesservice.dto.DisputeRequestDTO;
import org.mounanga.disputesservice.dto.DisputeResolutionRequestDTO;
import org.mounanga.disputesservice.dto.DisputeResponseDTO;
import org.mounanga.disputesservice.enums.Currency;
import org.mounanga.disputesservice.enums.DisputeReason;
import org.mounanga.disputesservice.enums.DisputeStatus;
import org.mounanga.disputesservice.exception.AccountNotFoundException;
import org.mounanga.disputesservice.exception.DisputeNotFoundException;
import org.mounanga.disputesservice.exception.GlobalExceptionHandler;
import org.mounanga.disputesservice.security.JWTAuthorizationFilter;
import org.mounanga.disputesservice.security.SecurityConfiguration;
import org.mounanga.disputesservice.service.DisputeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DisputeRestController.class)
@Import({SecurityConfiguration.class, JWTAuthorizationFilter.class, ApplicationProperties.class, GlobalExceptionHandler.class})
class DisputeRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DisputeService disputeService;

    @Test
    @WithMockUser
    void testOpenDispute() throws Exception {
        DisputeRequestDTO request = DisputeRequestDTO.builder()
                .accountId("ACC-1")
                .amount(new BigDecimal("49.99"))
                .reason(DisputeReason.UNAUTHORIZED_TRANSACTION)
                .description("I did not make this purchase")
                .build();

        DisputeResponseDTO response = DisputeResponseDTO.builder()
                .id("DSP-1").accountId("ACC-1").customerId("CUST-1")
                .amount(request.getAmount()).currency(Currency.EUR)
                .reason(request.getReason()).status(DisputeStatus.OPEN)
                .build();

        when(disputeService.openDispute(any(DisputeRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/disputes/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("DSP-1"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.customerId").value("CUST-1"));
    }

    @Test
    @WithMockUser
    void testOpenDisputeInvalidBody() throws Exception {
        DisputeRequestDTO request = DisputeRequestDTO.builder().amount(BigDecimal.ZERO).build();

        mockMvc.perform(post("/disputes/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @WithMockUser
    void testOpenDisputeAccountNotFound() throws Exception {
        DisputeRequestDTO request = DisputeRequestDTO.builder()
                .accountId("UNKNOWN").amount(BigDecimal.TEN).reason(DisputeReason.OTHER).build();

        when(disputeService.openDispute(any(DisputeRequestDTO.class)))
                .thenThrow(new AccountNotFoundException("Account with id UNKNOWN not found"));

        mockMvc.perform(post("/disputes/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Account with id UNKNOWN not found"));
    }

    @Test
    @WithMockUser
    void testGetDisputeById() throws Exception {
        when(disputeService.getDisputeById("DSP-1"))
                .thenReturn(DisputeResponseDTO.builder().id("DSP-1").status(DisputeStatus.OPEN).build());

        mockMvc.perform(get("/disputes/get/DSP-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("DSP-1"));
    }

    @Test
    @WithMockUser
    void testGetDisputeByIdNotFound() throws Exception {
        when(disputeService.getDisputeById("MISSING"))
                .thenThrow(new DisputeNotFoundException("Dispute with id MISSING not found"));

        mockMvc.perform(get("/disputes/get/MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Dispute with id MISSING not found"));
    }

    @Test
    @WithMockUser
    void testResolveDispute() throws Exception {
        DisputeResolutionRequestDTO request = DisputeResolutionRequestDTO.builder()
                .status(DisputeStatus.RESOLVED_CUSTOMER).resolutionNote("refunded").build();

        when(disputeService.resolveDispute(eq("DSP-1"), any(DisputeResolutionRequestDTO.class)))
                .thenReturn(DisputeResponseDTO.builder().id("DSP-1")
                        .status(DisputeStatus.RESOLVED_CUSTOMER).resolutionNote("refunded").build());

        mockMvc.perform(patch("/disputes/resolve/DSP-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED_CUSTOMER"))
                .andExpect(jsonPath("$.resolutionNote").value("refunded"));
    }

    @Test
    @WithMockUser
    void testResolveDisputeNotFound() throws Exception {
        DisputeResolutionRequestDTO request = DisputeResolutionRequestDTO.builder()
                .status(DisputeStatus.REJECTED).build();

        when(disputeService.resolveDispute(eq("MISSING"), any(DisputeResolutionRequestDTO.class)))
                .thenThrow(new DisputeNotFoundException("Dispute with id MISSING not found"));

        mockMvc.perform(patch("/disputes/resolve/MISSING")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void testUnauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/disputes/get/DSP-1"))
                .andExpect(status().isForbidden());
    }
}
