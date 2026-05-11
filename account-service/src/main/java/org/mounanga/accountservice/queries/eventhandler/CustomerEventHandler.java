package org.mounanga.accountservice.queries.eventhandler;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.mounanga.accountservice.queries.entity.Account;
import org.mounanga.accountservice.queries.reposiory.AccountRepository;
import org.mounanga.sharedevents.event.CustomerUpdatedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Slf4j
@Transactional
public class CustomerEventHandler {

    private final AccountRepository accountRepository;

    public CustomerEventHandler(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @EventHandler
    public void on(@NotNull CustomerUpdatedEvent event) {
        log.info("CustomerUpdatedEvent received for customer id: {}", event.getId());
        Optional<Account> accountOptional = accountRepository.findByCustomerId(event.getId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            account.setLastModifiedDate(event.getEventDate());
            account.setLastModifiedBy(event.getEventBy());
            accountRepository.save(account);
            log.info("Account projection updated for customer id: {}", event.getId());
        } else {
            log.warn("No account found for customer id: {}", event.getId());
        }
    }
}
