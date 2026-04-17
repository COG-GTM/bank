package org.mounanga.collectionsservice.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CollectionActionRecordedEvent extends ApplicationEvent {

    private final String delinquentAccountId;
    private final String accountId;
    private final String customerId;
    private final String actionType;
    private final String description;

    public CollectionActionRecordedEvent(Object source,
                                          String delinquentAccountId,
                                          String accountId,
                                          String customerId,
                                          String actionType,
                                          String description) {
        super(source);
        this.delinquentAccountId = delinquentAccountId;
        this.accountId = accountId;
        this.customerId = customerId;
        this.actionType = actionType;
        this.description = description;
    }
}
