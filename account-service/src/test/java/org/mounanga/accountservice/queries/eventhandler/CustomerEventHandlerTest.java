package org.mounanga.accountservice.queries.eventhandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mounanga.accountservice.common.enums.AccountStatus;
import org.mounanga.accountservice.common.enums.Currency;
import org.mounanga.accountservice.queries.entity.Account;
import org.mounanga.accountservice.queries.reposiory.AccountRepository;
import org.mounanga.sharedevents.event.CustomerUpdatedEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerEventHandlerTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CustomerEventHandler customerEventHandler;

    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        account = new Account();
        account.setId("accountId");
        account.setCustomerId("customerId");
        account.setStatus(AccountStatus.ACTIVATED);
        account.setBalance(BigDecimal.valueOf(1000));
        account.setCurrency(Currency.USD);
        account.setCreatedBy("system");
        account.setCreatedDate(LocalDateTime.now());
    }

    @Test
    void testCustomerUpdatedEventWithExistingAccount() {
        CustomerUpdatedEvent event = new CustomerUpdatedEvent(
                "customerId", LocalDateTime.now(), "system",
                "John", "Doe", "john.doe@example.com"
        );
        when(accountRepository.findByCustomerId("customerId")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        customerEventHandler.on(event);

        verify(accountRepository).findByCustomerId("customerId");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void testCustomerUpdatedEventWithNoAccount() {
        CustomerUpdatedEvent event = new CustomerUpdatedEvent(
                "nonExistentCustomer", LocalDateTime.now(), "system",
                "Jane", "Doe", "jane.doe@example.com"
        );
        when(accountRepository.findByCustomerId("nonExistentCustomer")).thenReturn(Optional.empty());

        customerEventHandler.on(event);

        verify(accountRepository).findByCustomerId("nonExistentCustomer");
        verify(accountRepository, never()).save(any(Account.class));
    }
}
