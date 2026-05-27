package org.mounanga.collectionsservice.common.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CollectionInitiatedEvent {
    private final String collectionId;
    private final String accountId;
    private final BigDecimal overdraftAmount;
    private final BigDecimal threshold;
    private final LocalDateTime initiatedAt;

    public CollectionInitiatedEvent(String collectionId, String accountId, BigDecimal overdraftAmount, BigDecimal threshold, LocalDateTime initiatedAt) {
        this.collectionId = collectionId;
        this.accountId = accountId;
        this.overdraftAmount = overdraftAmount;
        this.threshold = threshold;
        this.initiatedAt = initiatedAt;
    }

    public String getCollectionId() { return collectionId; }
    public String getAccountId() { return accountId; }
    public BigDecimal getOverdraftAmount() { return overdraftAmount; }
    public BigDecimal getThreshold() { return threshold; }
    public LocalDateTime getInitiatedAt() { return initiatedAt; }
}
