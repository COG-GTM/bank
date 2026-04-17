package org.mounanga.collectionsservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.mounanga.collectionsservice.enums.CollectionStatus;
import org.mounanga.collectionsservice.enums.DelinquencyBucket;
import org.mounanga.collectionsservice.enums.ProductType;
import org.mounanga.collectionsservice.enums.Region;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delinquent_accounts")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = "collectionActions")
public class DelinquentAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private BigDecimal outstandingBalance;

    @Column(nullable = false)
    private BigDecimal minimumPaymentDue;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate lastPaymentDate;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate dueDate;

    @Column(nullable = false)
    private int daysOverdue;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DelinquencyBucket delinquencyBucket;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Region region;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CollectionStatus collectionStatus;

    private String assignedAgentId;

    @OneToMany(mappedBy = "delinquentAccount", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<CollectionAction> collectionActions = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @CreatedBy
    private String createdBy;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

    @LastModifiedBy
    private String lastModifiedBy;

    public void addCollectionAction(CollectionAction action) {
        if (collectionActions == null) {
            collectionActions = new ArrayList<>();
        }
        if (action != null && !collectionActions.contains(action)) {
            action.setDelinquentAccount(this);
            collectionActions.add(action);
        }
    }
}
