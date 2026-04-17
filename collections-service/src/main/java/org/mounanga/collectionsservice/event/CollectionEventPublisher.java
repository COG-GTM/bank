package org.mounanga.collectionsservice.event;

import lombok.extern.slf4j.Slf4j;
import org.mounanga.collectionsservice.entity.CollectionAction;
import org.mounanga.collectionsservice.entity.DelinquentAccount;
import org.mounanga.collectionsservice.enums.CollectionStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CollectionEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public CollectionEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishStatusChanged(DelinquentAccount account, CollectionStatus previousStatus) {
        log.info("Publishing collection status changed event for account '{}': {} -> {}",
                account.getAccountId(), previousStatus, account.getCollectionStatus());
        CollectionStatusChangedEvent event = new CollectionStatusChangedEvent(
                this,
                account.getId(),
                account.getAccountId(),
                account.getCustomerId(),
                account.getEmail(),
                previousStatus,
                account.getCollectionStatus()
        );
        applicationEventPublisher.publishEvent(event);
    }

    public void publishAgentAssigned(DelinquentAccount account, String agentId) {
        log.info("Publishing agent assigned event for account '{}': agent '{}'",
                account.getAccountId(), agentId);
        AgentAssignedEvent event = new AgentAssignedEvent(
                this,
                account.getId(),
                account.getAccountId(),
                account.getCustomerId(),
                agentId
        );
        applicationEventPublisher.publishEvent(event);
    }

    public void publishActionRecorded(DelinquentAccount account, CollectionAction action) {
        log.info("Publishing collection action recorded event for account '{}': action '{}'",
                account.getAccountId(), action.getActionType());
        CollectionActionRecordedEvent event = new CollectionActionRecordedEvent(
                this,
                account.getId(),
                account.getAccountId(),
                account.getCustomerId(),
                action.getActionType(),
                action.getDescription()
        );
        applicationEventPublisher.publishEvent(event);
    }
}
