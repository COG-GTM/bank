package org.mounanga.collectionsservice.common.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountDebitedEvent {
    private final String id;
    private final LocalDateTime eventDate;
    private final String eventBy;
    private final BigDecimal amount;
    private final String type;
    private final String description;

    public AccountDebitedEvent() {
        this.id = null;
        this.eventDate = null;
        this.eventBy = null;
        this.amount = null;
        this.type = null;
        this.description = null;
    }

    public AccountDebitedEvent(String id, LocalDateTime eventDate, String eventBy, BigDecimal amount, String type, String description) {
        this.id = id;
        this.eventDate = eventDate;
        this.eventBy = eventBy;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    public String getId() { return id; }
    public LocalDateTime getEventDate() { return eventDate; }
    public String getEventBy() { return eventBy; }
    public BigDecimal getAmount() { return amount; }
    public String getType() { return type; }
    public String getDescription() { return description; }
}
