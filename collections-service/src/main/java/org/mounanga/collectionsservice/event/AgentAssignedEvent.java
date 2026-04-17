package org.mounanga.collectionsservice.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AgentAssignedEvent extends ApplicationEvent {

    private final String delinquentAccountId;
    private final String accountId;
    private final String customerId;
    private final String agentId;

    public AgentAssignedEvent(Object source,
                               String delinquentAccountId,
                               String accountId,
                               String customerId,
                               String agentId) {
        super(source);
        this.delinquentAccountId = delinquentAccountId;
        this.accountId = accountId;
        this.customerId = customerId;
        this.agentId = agentId;
    }
}
