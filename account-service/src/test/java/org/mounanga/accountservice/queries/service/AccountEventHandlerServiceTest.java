package org.mounanga.accountservice.queries.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mounanga.accountservice.common.enums.AccountStatus;
import org.mounanga.accountservice.common.enums.Currency;
import org.mounanga.accountservice.common.enums.OperationType;
import org.mounanga.accountservice.common.event.*;
import org.mounanga.accountservice.queries.entity.Account;
import org.mounanga.accountservice.queries.entity.Operation;
import org.mounanga.accountservice.queries.reposiory.AccountRepository;
import org.mounanga.accountservice.queries.reposiory.OperationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountEventHandlerServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OperationRepository operationRepository;

    @InjectMocks
    private AccountEventHandlerService accountEventHandlerService;

    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        account = new Account();
        account.setId("accountId");
        account.setCustomerId("customerId");
        account.setStatus(AccountStatus.CREATED);
        account.setBalance(BigDecimal.valueOf(1000));
        account.setCurrency(Currency.USD);
        account.setCreatedBy("system");
        account.setCreatedDate(LocalDateTime.now());
    }

    @Test
    void testHandleAccountCreatedEvent() {
        AccountCreatedEvent event = new AccountCreatedEvent(
                "accountId", LocalDateTime.now(), "system",
                AccountStatus.CREATED, BigDecimal.valueOf(1000), Currency.USD, "customerId"
        );
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountEventHandlerService.handleAccountCreatedEvent(event);
        assertNotNull(result);
        assertEquals(event.getId(), result.getId());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void testHandleAccountActivatedEvent() {
        AccountActivatedEvent event = new AccountActivatedEvent("accountId", LocalDateTime.now(), "system", AccountStatus.ACTIVATED);
        when(accountRepository.findById(event.getId())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountEventHandlerService.handleAccountActivatedEvent(event);
        assertNotNull(result);
        assertEquals(event.getId(), result.getId());
        assertEquals(event.getStatus(), result.getStatus());
        assertEquals(event.getEventBy(), result.getLastModifiedBy());
        assertEquals(event.getEventDate(), result.getLastModifiedDate());
    }

    @Test
    void testHandleAccountSuspendedEvent() {
        AccountSuspendedEvent event = new AccountSuspendedEvent("accountId", LocalDateTime.now(), "system", AccountStatus.SUSPENDED);
        when(accountRepository.findById(event.getId())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountEventHandlerService.handleAccountSuspendedEvent(event);
        assertNotNull(result);
        assertEquals(event.getId(), result.getId());
        assertEquals(event.getStatus(), result.getStatus());
        assertEquals(event.getEventBy(), result.getLastModifiedBy());
        assertEquals(event.getEventDate(), result.getLastModifiedDate());
    }

    @Test
    void testHandleAccountDeletedEvent() {
        AccountDeletedEvent event = new AccountDeletedEvent("accountId", LocalDateTime.now(), "system");
        account.setLastModifiedBy(event.getEventBy());
        when(accountRepository.findById(event.getId())).thenReturn(Optional.of(account));

        accountEventHandlerService.handleAccountDeletedEvent(event);

        verify(operationRepository).deleteByAccountId(account.getId());
        verify(accountRepository).deleteById(account.getId());
    }

    @Test
    void testHandleAccountCreditedEvent() {
        String accountId = "account";
        LocalDateTime eventDate = LocalDateTime.now();
        AccountCreditedEvent event = new AccountCreditedEvent(
                accountId, eventDate, "system",
                new BigDecimal("100.00"), OperationType.CREDIT, "Deposit"
        );
        account.setBalance(new BigDecimal("500.00"));

        when(accountRepository.findById(event.getId())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        Operation operation = new Operation();
        operation.setId("operationId");
        when(operationRepository.save(any(Operation.class))).thenReturn(operation);

        Operation result = accountEventHandlerService.handleAccountCreditedEvent(event);

        verify(accountRepository).findById(event.getId());
        verify(accountRepository).save(account);
        verify(operationRepository).save(any(Operation.class));
        assertEquals("operationId", result.getId());
    }

    @Test
    void testHandleAccountDebitedEvent() {
        String accountId = "account";
        LocalDateTime eventDate = LocalDateTime.now();
        AccountDebitedEvent event = new AccountDebitedEvent(
                accountId, eventDate, "system",
                new BigDecimal("100.00"), OperationType.DEBIT, "Withdrawal"
        );
        account.setBalance(new BigDecimal("500.00"));

        when(accountRepository.findById(event.getId())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        Operation operation = new Operation();
        operation.setId("operationId");
        when(operationRepository.save(any(Operation.class))).thenReturn(operation);

        Operation result = accountEventHandlerService.handleAccountDebitedEvent(event);

        verify(accountRepository).findById(event.getId());
        verify(accountRepository).save(account);
        verify(operationRepository).save(any(Operation.class));
        assertEquals("operationId", result.getId());
    }

}
