package org.mounanga.notificationservice.eventhandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mounanga.notificationservice.dto.CustomerResponseDTO;
import org.mounanga.notificationservice.dto.NotificationRequestDTO;
import org.mounanga.notificationservice.entity.AccountCustomerMapping;
import org.mounanga.notificationservice.repository.AccountCustomerMappingRepository;
import org.mounanga.notificationservice.service.NotificationService;
import org.mounanga.notificationservice.web.CustomerRestClient;
import org.mounanga.sharedevents.enums.AccountStatus;
import org.mounanga.sharedevents.enums.Currency;
import org.mounanga.sharedevents.enums.OperationType;
import org.mounanga.sharedevents.event.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountEventNotificationHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CustomerRestClient customerRestClient;

    @Mock
    private AccountCustomerMappingRepository mappingRepository;

    @InjectMocks
    private AccountEventNotificationHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAccountCreatedEventSendsNotification() {
        AccountCreatedEvent event = new AccountCreatedEvent(
                "acc123", LocalDateTime.now(), "system",
                AccountStatus.CREATED, BigDecimal.ZERO, Currency.USD, "cust123"
        );
        CustomerResponseDTO customer = new CustomerResponseDTO("cust123", "John", "Doe", "john@example.com");
        when(customerRestClient.getCustomerById("cust123")).thenReturn(customer);

        handler.on(event);

        verify(mappingRepository).save(any(AccountCustomerMapping.class));
        verify(customerRestClient).getCustomerById("cust123");
        verify(notificationService).send(any(NotificationRequestDTO.class));
    }

    @Test
    void testAccountCreatedEventNoEmailFound() {
        AccountCreatedEvent event = new AccountCreatedEvent(
                "acc123", LocalDateTime.now(), "system",
                AccountStatus.CREATED, BigDecimal.ZERO, Currency.USD, "cust123"
        );
        when(customerRestClient.getCustomerById("cust123")).thenReturn(null);

        handler.on(event);

        verify(mappingRepository).save(any(AccountCustomerMapping.class));
        verify(customerRestClient).getCustomerById("cust123");
        verify(notificationService, never()).send(any(NotificationRequestDTO.class));
    }

    @Test
    void testAccountActivatedEventSendsNotification() {
        AccountActivatedEvent event = new AccountActivatedEvent(
                "acc123", LocalDateTime.now(), "system", AccountStatus.ACTIVATED
        );
        when(mappingRepository.findById("acc123"))
                .thenReturn(Optional.of(new AccountCustomerMapping("acc123", "cust123")));
        when(customerRestClient.getCustomerById("cust123"))
                .thenReturn(new CustomerResponseDTO("cust123", "John", "Doe", "john@example.com"));

        handler.on(event);

        verify(notificationService).send(any(NotificationRequestDTO.class));
    }

    @Test
    void testAccountActivatedEventNoMappingFound() {
        AccountActivatedEvent event = new AccountActivatedEvent(
                "acc123", LocalDateTime.now(), "system", AccountStatus.ACTIVATED
        );
        when(mappingRepository.findById("acc123")).thenReturn(Optional.empty());

        handler.on(event);

        verify(notificationService, never()).send(any(NotificationRequestDTO.class));
    }

    @Test
    void testAccountSuspendedEventSendsNotification() {
        AccountSuspendedEvent event = new AccountSuspendedEvent(
                "acc123", LocalDateTime.now(), "system", AccountStatus.SUSPENDED
        );
        when(mappingRepository.findById("acc123"))
                .thenReturn(Optional.of(new AccountCustomerMapping("acc123", "cust123")));
        when(customerRestClient.getCustomerById("cust123"))
                .thenReturn(new CustomerResponseDTO("cust123", "John", "Doe", "john@example.com"));

        handler.on(event);

        verify(notificationService).send(any(NotificationRequestDTO.class));
    }

    @Test
    void testAccountDeletedEventCleansUpMapping() {
        AccountDeletedEvent event = new AccountDeletedEvent(
                "acc123", LocalDateTime.now(), "system"
        );
        when(mappingRepository.findById("acc123"))
                .thenReturn(Optional.of(new AccountCustomerMapping("acc123", "cust123")));
        when(customerRestClient.getCustomerById("cust123"))
                .thenReturn(new CustomerResponseDTO("cust123", "John", "Doe", "john@example.com"));

        handler.on(event);

        verify(notificationService).send(any(NotificationRequestDTO.class));
        verify(mappingRepository).deleteById("acc123");
    }

    @Test
    void testAccountCreditedEventSendsNotification() {
        AccountCreditedEvent event = new AccountCreditedEvent(
                "acc123", LocalDateTime.now(), "system",
                new BigDecimal("100.00"), OperationType.CREDIT, "Deposit"
        );
        when(mappingRepository.findById("acc123"))
                .thenReturn(Optional.of(new AccountCustomerMapping("acc123", "cust123")));
        when(customerRestClient.getCustomerById("cust123"))
                .thenReturn(new CustomerResponseDTO("cust123", "John", "Doe", "john@example.com"));

        handler.on(event);

        verify(notificationService).send(any(NotificationRequestDTO.class));
    }

    @Test
    void testAccountDebitedEventSendsNotification() {
        AccountDebitedEvent event = new AccountDebitedEvent(
                "acc123", LocalDateTime.now(), "system",
                new BigDecimal("50.00"), OperationType.DEBIT, "Withdrawal"
        );
        when(mappingRepository.findById("acc123"))
                .thenReturn(Optional.of(new AccountCustomerMapping("acc123", "cust123")));
        when(customerRestClient.getCustomerById("cust123"))
                .thenReturn(new CustomerResponseDTO("cust123", "John", "Doe", "john@example.com"));

        handler.on(event);

        verify(notificationService).send(any(NotificationRequestDTO.class));
    }

    @Test
    void testCustomerLookupFailureHandledGracefully() {
        AccountCreditedEvent event = new AccountCreditedEvent(
                "acc123", LocalDateTime.now(), "system",
                new BigDecimal("100.00"), OperationType.CREDIT, "Deposit"
        );
        when(mappingRepository.findById("acc123"))
                .thenReturn(Optional.of(new AccountCustomerMapping("acc123", "cust123")));
        when(customerRestClient.getCustomerById("cust123")).thenThrow(new RuntimeException("Service unavailable"));

        handler.on(event);

        verify(notificationService, never()).send(any(NotificationRequestDTO.class));
    }
}
