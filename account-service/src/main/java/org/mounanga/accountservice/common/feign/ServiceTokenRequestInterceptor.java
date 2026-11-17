package org.mounanga.accountservice.common.feign;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;

import java.time.Duration;
import java.time.Instant;

public class ServiceTokenRequestInterceptor implements RequestInterceptor {

    public static final String SERVICE_ROLE = "SERVICE";
    private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(5);

    private final Algorithm algorithm;
    private final String serviceName;

    public ServiceTokenRequestInterceptor(String jwtSecret, String serviceName) {
        this.algorithm = Algorithm.HMAC256(jwtSecret);
        this.serviceName = serviceName;
    }

    @Override
    public void apply(RequestTemplate template) {
        String token = JWT.create()
                .withSubject(serviceName)
                .withArrayClaim("roles", new String[]{SERVICE_ROLE})
                .withExpiresAt(Instant.now().plus(TOKEN_LIFETIME))
                .sign(algorithm);
        template.removeHeader(HttpHeaders.AUTHORIZATION);
        template.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
}
