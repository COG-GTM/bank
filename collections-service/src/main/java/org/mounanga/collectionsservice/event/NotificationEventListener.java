package org.mounanga.collectionsservice.event;

import lombok.extern.slf4j.Slf4j;
import org.mounanga.collectionsservice.dto.NotificationRequestDTO;
import org.mounanga.collectionsservice.web.NotificationRestClient;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class NotificationEventListener {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm");
    private static final String STATUS_CHANGE_SUBJECT = "Collections: status update on your account";
    private static final String AGENT_ASSIGNED_SUBJECT = "Collections: an agent has been assigned to your account";
    private static final String ACTION_RECORDED_SUBJECT = "Collections: a new action has been recorded on your account";

    private final NotificationRestClient notificationRestClient;

    public NotificationEventListener(NotificationRestClient notificationRestClient) {
        this.notificationRestClient = notificationRestClient;
    }

    @Async
    @EventListener
    public void onStatusChanged(DelinquentAccountStatusChangedEvent event) {
        log.info("DelinquentAccountStatusChangedEvent received for account '{}': {} -> {}",
                event.getAccountNumber(), event.getPreviousStatus(), event.getNewStatus());
        String body = String.format(
                "Hello! The collection status on your account '%s' changed from %s to %s on %s.%s",
                event.getAccountNumber(),
                event.getPreviousStatus(),
                event.getNewStatus(),
                event.getOccurredAt().format(FORMATTER),
                event.getReason() == null || event.getReason().isBlank() ? "" : " Reason: " + event.getReason() + ".");
        safeSend(event.getAccountHolderEmail(), STATUS_CHANGE_SUBJECT, body);
    }

    @Async
    @EventListener
    public void onAgentAssigned(AgentAssignedEvent event) {
        log.info("AgentAssignedEvent received for account '{}': agent '{}'", event.getAccountNumber(), event.getAgentId());
        String body = String.format(
                "Hello! Agent '%s' has been assigned to your account '%s' on %s. They will reach out to you soon.",
                event.getAgentId(),
                event.getAccountNumber(),
                event.getOccurredAt().format(FORMATTER));
        safeSend(event.getAccountHolderEmail(), AGENT_ASSIGNED_SUBJECT, body);
    }

    @Async
    @EventListener
    public void onActionRecorded(CollectionActionRecordedEvent event) {
        log.info("CollectionActionRecordedEvent received for account '{}': {} / {}",
                event.getAccountNumber(), event.getType(), event.getOutcome());
        String body = String.format(
                "Hello! A new collection action was recorded on your account '%s' on %s (type: %s, outcome: %s).",
                event.getAccountNumber(),
                event.getOccurredAt().format(FORMATTER),
                event.getType(),
                event.getOutcome());
        safeSend(event.getAccountHolderEmail(), ACTION_RECORDED_SUBJECT, body);
    }

    private void safeSend(String to, String subject, String body) {
        try {
            notificationRestClient.sendNotification(new NotificationRequestDTO(to, subject, body));
        } catch (Exception e) {
            log.warn("Failed to dispatch notification for '{}': {}", to, e.getMessage());
        }
    }
}
