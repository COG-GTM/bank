package org.mounanga.customerservice.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtSecretConfigurationTest {

    @Test
    void jwtSecretComesOnlyFromJwtSecretEnvironmentVariable() throws IOException {
        String secret = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"))
                .getProperty("application.security.jwt.secret");

        assertEquals("${JWT_SECRET}", secret);
    }
}
