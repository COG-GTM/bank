package org.mounanga.sharedevents.event;

import lombok.Getter;
import org.mounanga.sharedevents.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class AccountDebitedEvent extends BaseEvent<String> {
    private final BigDecimal amount;
    private final OperationType type;
    private final String description;

    public AccountDebitedEvent(String id, LocalDateTime eventDate, String eventBy, BigDecimal amount, OperationType type, String description) {
        super(id, eventDate, eventBy);
        this.amount = amount;
        this.type = type;
        this.description = description;
    }
}
