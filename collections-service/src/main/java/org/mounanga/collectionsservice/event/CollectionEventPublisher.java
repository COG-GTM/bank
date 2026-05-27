package org.mounanga.collectionsservice.event;

import org.mounanga.collectionsservice.common.event.CollectionInitiatedEvent;

public interface CollectionEventPublisher {
    void publish(CollectionInitiatedEvent event);
}
