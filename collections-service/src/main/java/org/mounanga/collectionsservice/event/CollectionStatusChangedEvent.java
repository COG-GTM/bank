package org.mounanga.collectionsservice.event;

import lombok.Getter;
import org.mounanga.collectionsservice.enums.CollectionStatus;
import org.springframework.context.ApplicationEvent;

@Getter
public class CollectionStatusChangedEvent extends ApplicationEvent {

    private final String delinquentAccountId;
    private final String accountId;
    private final String customerId;
    private final String email;
    private final CollectionStatus previousStatus;
    private final CollectionStatus newStatus;

    public CollectionStatusChangedEvent(Object source,
                                         String delinquentAccountId,
                                         String accountId,
                                         String customerId,
                                         String email,
                                         CollectionStatus previousStatus,
                                         CollectionStatus newStatus) {
        super(source);
        this.delinquentAccountId = delinquentAccountId;
        this.accountId = accountId;
        this.customerId = customerId;
        this.email = email;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }
}
