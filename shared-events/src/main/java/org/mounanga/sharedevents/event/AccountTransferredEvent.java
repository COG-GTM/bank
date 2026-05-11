package org.mounanga.sharedevents.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class AccountTransferredEvent extends BaseEvent<String> {
    private final String accountIdFrom;
    private final String accountIdTo;
    private final BigDecimal amount;
    private final String description;

    public AccountTransferredEvent(String id, LocalDateTime eventDate, String eventBy, String accountIdFrom, String accountIdTo, BigDecimal amount, String description) {
        super(id, eventDate, eventBy);
        this.accountIdFrom = accountIdFrom;
        this.accountIdTo = accountIdTo;
        this.amount = amount;
        this.description = description;
    }
}
