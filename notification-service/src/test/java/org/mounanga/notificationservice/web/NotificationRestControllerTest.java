package org.mounanga.notificationservice.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.mounanga.notificationservice.configuration.ApplicationProperties;
import org.mounanga.notificationservice.dto.NotificationRequestDTO;
import org.mounanga.notificationservice.security.JWTAuthorizationFilter;
import org.mounanga.notificationservice.security.SecurityConfiguration;
import org.mounanga.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationRestController.class)
@Import({SecurityConfiguration.class, JWTAuthorizationFilter.class, ApplicationProperties.class})
class NotificationRestControllerTest {

    private static final String BODY = "{\"to\":\"alice@example.com\",\"subject\":\"Sample\",\"body\":\"Sample body\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Value("${application.security.jwt.secret}")
    private String jwtSecret;

    @Test
    void rejectsSendWithoutToken() throws Exception {
        mockMvc.perform(send()).andExpect(status().isForbidden());

        verifyNoInteractions(notificationService);
    }

    @Test
    void rejectsSendWithUserToken() throws Exception {
        mockMvc.perform(send().header("Authorization", "Bearer " + token("alice", "USER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(notificationService);
    }

    @Test
    void rejectsSendWithInvalidToken() throws Exception {
        mockMvc.perform(send().header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(notificationService);
    }

    @Test
    void acceptsSendWithServiceToken() throws Exception {
        mockMvc.perform(send().header("Authorization", "Bearer " + token("account-service", SecurityConfiguration.SERVICE_ROLE)))
                .andExpect(status().isOk());

        verify(notificationService).send(new NotificationRequestDTO("alice@example.com", "Sample", "Sample body"));
    }

    private MockHttpServletRequestBuilder send() {
        return post("/bank/notifications/send").contextPath("/bank")
                .contentType(MediaType.APPLICATION_JSON).content(BODY);
    }

    private String token(String subject, String role) {
        return JWT.create().withSubject(subject).withArrayClaim("roles", new String[]{role})
                .withExpiresAt(Instant.now().plusSeconds(60)).sign(Algorithm.HMAC256(jwtSecret));
    }
}
