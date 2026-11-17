package org.mounanga.customerservice.web;

import org.mounanga.customerservice.dto.CustomerExistResponseDTO;
import org.mounanga.customerservice.service.CustomerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/intern")
public class CustomerInternRestController {

    private final CustomerService customerService;

    public CustomerInternRestController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/verify/{id}")
    public CustomerExistResponseDTO verifyCustomerExist(@PathVariable String id) {
        return customerService.verifyCustomerExist(id);
    }
}
