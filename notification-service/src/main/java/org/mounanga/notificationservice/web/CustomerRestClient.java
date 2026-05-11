package org.mounanga.notificationservice.web;

import org.mounanga.notificationservice.dto.CustomerResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CUSTOMER-SERVICE")
public interface CustomerRestClient {

    @GetMapping("/bank/customers/get/{id}")
    CustomerResponseDTO getCustomerById(@PathVariable String id);
}
