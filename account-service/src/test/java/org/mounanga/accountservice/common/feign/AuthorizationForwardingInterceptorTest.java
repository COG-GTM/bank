package org.mounanga.accountservice.common.feign;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AuthorizationForwardingInterceptorTest {

    private final AuthorizationForwardingInterceptor interceptor = new AuthorizationForwardingInterceptor();

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void forwardsIncomingAuthorizationHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abc.def.ghi");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertEquals(List.of("Bearer abc.def.ghi"), List.copyOf(template.headers().get("Authorization")));
    }

    @Test
    void addsNothingOutsideARequest() {
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertFalse(template.headers().containsKey("Authorization"));
    }
}
