package org.mounanga.collectionsservice.event;

import lombok.Getter;
import lombok.ToString;
import org.mounanga.collectionsservice.enums.ActionOutcome;
import org.mounanga.collectionsservice.enums.ActionType;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
@ToString
public class CollectionActionRecordedEvent extends ApplicationEvent {

    private final String delinquentAccountId;
    private final String accountNumber;
    private final String accountHolderEmail;
    private final ActionType type;
    private final ActionOutcome outcome;
    private final String agentId;
    private final LocalDateTime occurredAt;

    public CollectionActionRecordedEvent(Object source,
                                         String delinquentAccountId,
                                         String accountNumber,
                                         String accountHolderEmail,
                                         ActionType type,
                                         ActionOutcome outcome,
                                         String agentId,
                                         LocalDateTime occurredAt) {
        super(source);
        this.delinquentAccountId = delinquentAccountId;
        this.accountNumber = accountNumber;
        this.accountHolderEmail = accountHolderEmail;
        this.type = type;
        this.outcome = outcome;
        this.agentId = agentId;
        this.occurredAt = occurredAt;
    }
}
