package org.mounanga.accountservice.common.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forwards the caller's JWT on Feign calls so token-protected internal routes
 * (e.g. customer-service /intern/**) accept service-to-service requests.
 */
@Component
public class AuthorizationForwardingInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && !template.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
            template.header(HttpHeaders.AUTHORIZATION, authorization);
        }
    }
}
