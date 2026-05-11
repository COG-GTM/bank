package org.mounanga.notificationservice.eventhandler;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import lombok.NonNull;
import org.mounanga.notificationservice.dto.CustomerResponseDTO;
import org.mounanga.notificationservice.dto.NotificationRequestDTO;
import org.mounanga.notificationservice.entity.AccountCustomerMapping;
import org.mounanga.notificationservice.repository.AccountCustomerMappingRepository;
import org.mounanga.notificationservice.service.NotificationService;
import org.mounanga.notificationservice.web.CustomerRestClient;
import org.mounanga.sharedevents.event.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
@Slf4j
@Transactional
public class AccountEventNotificationHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm");

    private final NotificationService notificationService;
    private final CustomerRestClient customerRestClient;
    private final AccountCustomerMappingRepository mappingRepository;

    public AccountEventNotificationHandler(NotificationService notificationService,
                                           CustomerRestClient customerRestClient,
                                           AccountCustomerMappingRepository mappingRepository) {
        this.notificationService = notificationService;
        this.customerRestClient = customerRestClient;
        this.mappingRepository = mappingRepository;
    }

    @EventHandler
    public void on(@NonNull AccountCreatedEvent event) {
        log.info("AccountCreatedEvent received for notification, account id: {}", event.getId());
        mappingRepository.save(new AccountCustomerMapping(event.getId(), event.getCustomerId()));
        String email = lookupCustomerEmail(event.getCustomerId());
        if (email == null) {
            log.warn("Could not find email for customer id: {}", event.getCustomerId());
            return;
        }
        String body = "Hello! Your bank account has been successfully created on " + formatDateTime(event.getEventDate()) +
                ". Your bank account number is " + event.getId() + ".";
        sendNotification(email, "Bank account creation.", body);
    }

    @EventHandler
    public void on(@NonNull AccountActivatedEvent event) {
        log.info("AccountActivatedEvent received for notification, account id: {}", event.getId());
        String email = lookupEmailByAccountId(event.getId());
        if (email == null) return;
        String body = "Hello! Your bank account has just been activated on " + formatDateTime(event.getEventDate()) +
                ". For further information, please contact your nearest branch.";
        sendNotification(email, "Bank account activation.", body);
    }

    @EventHandler
    public void on(@NonNull AccountSuspendedEvent event) {
        log.info("AccountSuspendedEvent received for notification, account id: {}", event.getId());
        String email = lookupEmailByAccountId(event.getId());
        if (email == null) return;
        String body = "Hello! Your bank account has just been suspended on " + formatDateTime(event.getEventDate()) +
                ". For further information, please contact your nearest branch.";
        sendNotification(email, "Bank account suspension.", body);
    }

    @EventHandler
    public void on(@NonNull AccountDeletedEvent event) {
        log.info("AccountDeletedEvent received for notification, account id: {}", event.getId());
        String email = lookupEmailByAccountId(event.getId());
        if (email != null) {
            String body = "Hello! Your account with id " + event.getId() + " has just been deleted on " + formatDateTime(event.getEventDate()) +
                    ". For further information, please contact your nearest branch.";
            sendNotification(email, "Bank account deleted.", body);
        }
        mappingRepository.deleteById(event.getId());
    }

    @EventHandler
    public void on(@NonNull AccountCreditedEvent event) {
        log.info("AccountCreditedEvent received for notification, account id: {}", event.getId());
        String email = lookupEmailByAccountId(event.getId());
        if (email == null) return;
        String body = "Hello! Your bank account was credited with " + event.getAmount() +
                " on " + formatDateTime(event.getEventDate()) + ".";
        sendNotification(email, "Bank account credited.", body);
    }

    @EventHandler
    public void on(@NonNull AccountDebitedEvent event) {
        log.info("AccountDebitedEvent received for notification, account id: {}", event.getId());
        String email = lookupEmailByAccountId(event.getId());
        if (email == null) return;
        String body = "Hello! Your bank account was debited with " + event.getAmount() +
                " on " + formatDateTime(event.getEventDate()) + ".";
        sendNotification(email, "Bank account debited.", body);
    }

    private String lookupEmailByAccountId(String accountId) {
        Optional<AccountCustomerMapping> mapping = mappingRepository.findById(accountId);
        if (mapping.isEmpty()) {
            log.warn("No customer mapping found for account id: {}", accountId);
            return null;
        }
        return lookupCustomerEmail(mapping.get().getCustomerId());
    }

    private String lookupCustomerEmail(String customerId) {
        try {
            CustomerResponseDTO customer = customerRestClient.getCustomerById(customerId);
            return customer != null ? customer.email() : null;
        } catch (Exception e) {
            log.error("Failed to look up customer email for customer id: {}", customerId, e);
            return null;
        }
    }

    private void sendNotification(String email, String subject, String body) {
        NotificationRequestDTO notification = new NotificationRequestDTO(email, subject, body);
        notificationService.send(notification);
    }

    private String formatDateTime(@NonNull LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }
}
