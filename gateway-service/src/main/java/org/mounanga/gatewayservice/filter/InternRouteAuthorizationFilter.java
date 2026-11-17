package org.mounanga.gatewayservice.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

/**
 * Rejects routed calls to internal ({@code /intern/**}) service endpoints that carry no bearer token.
 * The downstream service still validates the token itself.
 */
@Component
public class InternRouteAuthorizationFilter implements GlobalFilter, Ordered {

    private static final Pattern INTERN_SEGMENT = Pattern.compile("(^|/)intern(/|$)", Pattern.CASE_INSENSITIVE);
    private static final String BEARER = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (INTERN_SEGMENT.matcher(path).find() && !hasBearerToken(exchange)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    private boolean hasBearerToken(ServerWebExchange exchange) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return authorization != null && authorization.startsWith(BEARER) && authorization.length() > BEARER.length();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
