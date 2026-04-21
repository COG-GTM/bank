package org.mounanga.collectionsservice.event;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
@ToString
public class AgentAssignedEvent extends ApplicationEvent {

    private final String delinquentAccountId;
    private final String accountNumber;
    private final String accountHolderEmail;
    private final String agentId;
    private final LocalDateTime occurredAt;

    public AgentAssignedEvent(Object source,
                              String delinquentAccountId,
                              String accountNumber,
                              String accountHolderEmail,
                              String agentId,
                              LocalDateTime occurredAt) {
        super(source);
        this.delinquentAccountId = delinquentAccountId;
        this.accountNumber = accountNumber;
        this.accountHolderEmail = accountHolderEmail;
        this.agentId = agentId;
        this.occurredAt = occurredAt;
    }
}
