package org.mounanga.collectionsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CollectionsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectionsServiceApplication.class, args);
    }
}
