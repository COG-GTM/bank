package org.mounanga.gatewayservice.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
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

class NotificationRouteAuthorizationFilterTest {

    private static final String SEND_PATH = "/NOTIFICATION-SERVICE/bank/notifications/send";

    private final NotificationRouteAuthorizationFilter filter = new NotificationRouteAuthorizationFilter();

    @Test
    void rejectsNotificationCallWithoutToken() {
        assertRejected(MockServerHttpRequest.post(SEND_PATH).build());
    }

    @Test
    void rejectsPercentEncodedNotificationCallWithoutToken() {
        assertRejected(MockServerHttpRequest.method(HttpMethod.POST,
                URI.create("/%4EOTIFICATION-SERVICE/bank/notifications/send")).build());
    }

    @Test
    void rejectsServiceIdWithPathParameterWithoutToken() {
        assertRejected(MockServerHttpRequest.post("/NOTIFICATION-SERVICE;x=1/bank/notifications/send").build());
    }

    @Test
    void rejectsLowercaseServiceIdWithoutToken() {
        assertRejected(MockServerHttpRequest.post("/notification-service/bank/notifications/send").build());
    }

    @Test
    void forwardsNotificationCallWithBearerToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post(SEND_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi").build());
        AtomicBoolean forwarded = new AtomicBoolean();

        filter.filter(exchange, chain(forwarded)).block();

        assertTrue(forwarded.get());
    }

    @Test
    void forwardsOtherServicesNotificationsPathsWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/ACCOUNT-SERVICE/bank/notifications").build());
        AtomicBoolean forwarded = new AtomicBoolean();

        filter.filter(exchange, chain(forwarded)).block();

        assertTrue(forwarded.get());
    }

    private void assertRejected(MockServerHttpRequest request) {
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean forwarded = new AtomicBoolean();

        filter.filter(exchange, chain(forwarded)).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertFalse(forwarded.get());
    }

    private GatewayFilterChain chain(AtomicBoolean forwarded) {
        return exchange -> {
            forwarded.set(true);
            return Mono.empty();
        };
    }
}
