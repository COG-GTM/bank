package org.mounanga.collectionsservice.event;

import lombok.extern.slf4j.Slf4j;
import org.mounanga.collectionsservice.enums.CollectionStatus;
import org.mounanga.collectionsservice.web.NotificationRestClient;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CollectionEventListener {

    private final NotificationRestClient notificationRestClient;

    public CollectionEventListener(NotificationRestClient notificationRestClient) {
        this.notificationRestClient = notificationRestClient;
    }

    @Async
    @EventListener
    public void handleStatusChanged(CollectionStatusChangedEvent event) {
        log.info("Handling CollectionStatusChangedEvent: account '{}' status {} -> {}",
                event.getAccountId(), event.getPreviousStatus(), event.getNewStatus());

        if (event.getNewStatus() == CollectionStatus.ESCALATED) {
            log.warn("Account '{}' has been ESCALATED - triggering escalation notification", event.getAccountId());
            sendNotification(event.getEmail(), "Collection Escalation Notice",
                    String.format("Account %s has been escalated for immediate attention. Previous status: %s",
                            event.getAccountId(), event.getPreviousStatus()));
        } else if (event.getNewStatus() == CollectionStatus.PAYMENT_ARRANGED) {
            log.info("Payment arrangement confirmed for account '{}'", event.getAccountId());
            sendNotification(event.getEmail(), "Payment Arrangement Confirmed",
                    String.format("A payment arrangement has been set up for account %s. Thank you for your cooperation.",
                            event.getAccountId()));
        }
    }

    @Async
    @EventListener
    public void handleAgentAssigned(AgentAssignedEvent event) {
        log.info("Handling AgentAssignedEvent: agent '{}' assigned to account '{}'",
                event.getAgentId(), event.getAccountId());
    }

    @Async
    @EventListener
    public void handleActionRecorded(CollectionActionRecordedEvent event) {
        log.info("Handling CollectionActionRecordedEvent: action '{}' for account '{}'",
                event.getActionType(), event.getAccountId());
    }

    private void sendNotification(String email, String subject, String body) {
        try {
            notificationRestClient.sendNotification(email, subject, body);
        } catch (Exception e) {
            log.error("Failed to send notification to '{}': {}", email, e.getMessage());
        }
    }
}
