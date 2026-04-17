package org.mounanga.collectionsservice.web;

import org.mounanga.collectionsservice.dto.AccountInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ACCOUNT-SERVICE", path = "/bank/accounts/queries")
public interface AccountRestClient {

    @GetMapping("/get-account/{id}")
    AccountInfoDTO getAccountById(@PathVariable("id") String id);

    @GetMapping("/find-account/{customerId}")
    AccountInfoDTO getAccountByCustomerId(@PathVariable("customerId") String customerId);
}
