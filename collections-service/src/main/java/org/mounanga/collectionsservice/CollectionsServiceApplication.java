package org.mounanga.collectionsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableFeignClients
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class CollectionsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectionsServiceApplication.class, args);
    }
}
