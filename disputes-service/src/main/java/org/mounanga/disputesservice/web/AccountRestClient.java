package org.mounanga.disputesservice.web;

import org.mounanga.disputesservice.dto.AccountResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ACCOUNT-SERVICE")
public interface AccountRestClient {

    @GetMapping("/bank/accounts/queries/get-account/{id}")
    AccountResponseDTO getAccountById(@PathVariable String id);
}
