package org.mounanga.collectionsservice.event;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.mounanga.collectionsservice.common.event.CollectionInitiatedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CollectionEventPublisherImpl implements CollectionEventPublisher {

    private final EventGateway eventGateway;

    public CollectionEventPublisherImpl(EventGateway eventGateway) {
        this.eventGateway = eventGateway;
    }

    @Override
    public void publish(CollectionInitiatedEvent event) {
        log.info("Publishing CollectionInitiatedEvent for account '{}', collectionId '{}'", event.getAccountId(), event.getCollectionId());
        eventGateway.publish(event);
    }
}
