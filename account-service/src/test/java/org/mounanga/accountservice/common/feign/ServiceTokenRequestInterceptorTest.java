package org.mounanga.accountservice.common.feign;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceTokenRequestInterceptorTest {

    private static final String SECRET = "sample-secret";

    @Test
    void addsSignedServiceTokenReplacingAnyExistingAuthorization() {
        RequestTemplate template = new RequestTemplate();
        template.header(HttpHeaders.AUTHORIZATION, "Bearer user-token");

        new ServiceTokenRequestInterceptor(SECRET, "account-service").apply(template);

        Collection<String> values = template.headers().get(HttpHeaders.AUTHORIZATION);
        assertEquals(1, values.size());
        String header = values.iterator().next();
        assertTrue(header.startsWith("Bearer "));
        DecodedJWT jwt = JWT.require(Algorithm.HMAC256(SECRET)).build().verify(header.substring(7));
        assertEquals("account-service", jwt.getSubject());
        assertEquals(List.of(ServiceTokenRequestInterceptor.SERVICE_ROLE), jwt.getClaim("roles").asList(String.class));
        assertTrue(jwt.getExpiresAt().toInstant().isAfter(java.time.Instant.now()));
    }
}
