package org.mounanga.accountservice.commands.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.mounanga.accountservice.commands.dto.CustomerExistResponseDTO;
import org.mounanga.accountservice.commands.util.generator.IdGenerator;
import org.mounanga.accountservice.common.properties.ApplicationProperties;
import org.mounanga.accountservice.common.security.JWTAuthorizationFilter;
import org.mounanga.accountservice.common.security.SecurityConfiguration;
import org.mounanga.accountservice.common.security.SecurityInformationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountCommandRestController.class)
@Import({SecurityConfiguration.class, JWTAuthorizationFilter.class, ApplicationProperties.class, SecurityInformationImpl.class})
class AccountCommandRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommandGateway commandGateway;

    @MockBean
    private CustomerRestClient customerRestClient;

    @MockBean
    private IdGenerator idGenerator;

    @Value("${application.security.jwt.secret}")
    private String jwtSecret;

    @Test
    void createAccountReturnsNewIdToAuthenticatedCaller() throws Exception {
        when(customerRestClient.checkCustomerExist("c-1")).thenReturn(new CustomerExistResponseDTO("c-1", "alice@example.com"));
        when(idGenerator.autoGenerateId()).thenReturn("000000042");
        when(commandGateway.send(any())).thenReturn(CompletableFuture.completedFuture("000000042"));

        MvcResult started = mockMvc.perform(post("/accounts/commands/create")
                        .header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"c-1\",\"currency\":\"EUR\"}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(started))
                .andExpect(status().isOk())
                .andExpect(content().string("000000042"));
    }

    private String token() {
        return JWT.create()
                .withSubject("admin")
                .withClaim("roles", List.of("ADMIN", "USER"))
                .withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
                .sign(Algorithm.HMAC256(jwtSecret));
    }
}
