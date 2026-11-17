package org.mounanga.customerservice.web;

import org.junit.jupiter.api.Test;
import org.mounanga.customerservice.configuration.ApplicationProperties;
import org.mounanga.customerservice.dto.CustomerExistResponseDTO;
import org.mounanga.customerservice.excetion.CustomerNotFoundException;
import org.mounanga.customerservice.security.JWTAuthorizationFilter;
import org.mounanga.customerservice.security.SecurityConfiguration;
import org.mounanga.customerservice.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * account-service's CustomerRestClient calls GET /bank/intern/verify/{id} without a JWT
 * before creating an account; this endpoint must exist and stay open to internal calls.
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

    @Test
    void verifyReturnsCustomerIdAndEmailWithoutToken() throws Exception {
        when(customerService.verifyCustomerExist("c-1"))
                .thenReturn(new CustomerExistResponseDTO("c-1", "alice@example.com"));

        mockMvc.perform(get("/bank/intern/verify/c-1").contextPath("/bank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c-1"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void verifyPropagatesNotFoundForUnknownCustomer() {
        when(customerService.verifyCustomerExist("missing"))
                .thenThrow(new CustomerNotFoundException("Customer with id missing not found"));

        Exception thrown = assertThrows(Exception.class, () ->
                mockMvc.perform(get("/bank/intern/verify/missing").contextPath("/bank")));
        assertInstanceOf(CustomerNotFoundException.class, thrown.getCause());
    }
}
