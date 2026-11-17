package org.mounanga.customerservice.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.mounanga.customerservice.configuration.ApplicationProperties;
import org.mounanga.customerservice.dto.CustomerExistResponseDTO;
import org.mounanga.customerservice.excetion.CustomerNotFoundException;
import org.mounanga.customerservice.security.JWTAuthorizationFilter;
import org.mounanga.customerservice.security.SecurityConfiguration;
import org.mounanga.customerservice.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * account-service's CustomerRestClient calls GET /bank/intern/verify/{id} (forwarding the caller's JWT)
 * before creating an account. The route must answer authenticated callers, reject callers without a
 * token, and return 404 for unknown customers.
 */
@WebMvcTest(CustomerInternRestController.class)
@Import({SecurityConfiguration.class, JWTAuthorizationFilter.class, ApplicationProperties.class})
class CustomerInternRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockBean(name = "auditorAware")
    private AuditorAware<String> auditorAware;

    @Value("${application.security.jwt.secret}")
    private String jwtSecret;

    @Test
    void verifyReturnsCustomerIdAndEmailToAuthenticatedCaller() throws Exception {
        when(customerService.verifyCustomerExist("c-1"))
                .thenReturn(new CustomerExistResponseDTO("c-1", "alice@example.com"));

        mockMvc.perform(get("/bank/intern/verify/c-1").contextPath("/bank")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c-1"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void verifyRejectsCallerWithoutToken() throws Exception {
        mockMvc.perform(get("/bank/intern/verify/c-1").contextPath("/bank"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(customerService);
    }

    @Test
    void verifyReturnsNotFoundForUnknownCustomer() throws Exception {
        when(customerService.verifyCustomerExist("missing"))
                .thenThrow(new CustomerNotFoundException("Customer with id missing not found"));

        mockMvc.perform(get("/bank/intern/verify/missing").contextPath("/bank")
                        .header("Authorization", "Bearer " + token()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer with id missing not found"));
    }

    private String token() {
        return JWT.create()
                .withSubject("account-service-caller")
                .withClaim("roles", List.of("USER"))
                .withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
                .sign(Algorithm.HMAC256(jwtSecret));
    }
}
