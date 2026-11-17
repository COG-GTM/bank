package org.mounanga.gatewayservice.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternRouteAuthorizationFilterTest {

    private final InternRouteAuthorizationFilter filter = new InternRouteAuthorizationFilter();

    @Test
    void rejectsInternCallWithoutToken() {
        AtomicBoolean forwarded = new AtomicBoolean(false);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/CUSTOMER-SERVICE/bank/intern/verify/c-1"));

        filter.filter(exchange, chain(forwarded)).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertFalse(forwarded.get());
    }

    @Test
    void rejectsPercentEncodedInternCallWithoutToken() {
        AtomicBoolean forwarded = new AtomicBoolean(false);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.GET, URI.create("/CUSTOMER-SERVICE/bank/%69ntern/verify/c-1")));

        filter.filter(exchange, chain(forwarded)).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertFalse(forwarded.get());
    }

    @Test
    void forwardsInternCallWithBearerToken() {
        AtomicBoolean forwarded = new AtomicBoolean(false);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/CUSTOMER-SERVICE/bank/intern/verify/c-1")
                        .header("Authorization", "Bearer abc.def.ghi"));

        filter.filter(exchange, chain(forwarded)).block();

        assertTrue(forwarded.get());
    }

    @Test
    void leavesOtherRoutesToTheServices() {
        AtomicBoolean forwarded = new AtomicBoolean(false);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/ACCOUNT-SERVICE/bank/accounts/queries/find-account/c-1"));

        filter.filter(exchange, chain(forwarded)).block();

        assertTrue(forwarded.get());
    }

    private GatewayFilterChain chain(AtomicBoolean forwarded) {
        return exchange -> {
            forwarded.set(true);
            return Mono.empty();
        };
    }
}
