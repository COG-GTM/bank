package org.mounanga.collectionsservice.event;

import lombok.Getter;
import lombok.ToString;
import org.mounanga.collectionsservice.enums.CollectionStatus;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
@ToString
public class DelinquentAccountStatusChangedEvent extends ApplicationEvent {

    private final String delinquentAccountId;
    private final String accountNumber;
    private final String accountHolderEmail;
    private final CollectionStatus previousStatus;
    private final CollectionStatus newStatus;
    private final String reason;
    private final LocalDateTime occurredAt;

    public DelinquentAccountStatusChangedEvent(Object source,
                                               String delinquentAccountId,
                                               String accountNumber,
                                               String accountHolderEmail,
                                               CollectionStatus previousStatus,
                                               CollectionStatus newStatus,
                                               String reason,
                                               LocalDateTime occurredAt) {
        super(source);
        this.delinquentAccountId = delinquentAccountId;
        this.accountNumber = accountNumber;
        this.accountHolderEmail = accountHolderEmail;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.occurredAt = occurredAt;
    }
}
