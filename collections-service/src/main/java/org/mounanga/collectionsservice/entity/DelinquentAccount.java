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
@Table(name = "delinquent_account")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class DelinquentAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String accountHolderName;

    @Column(nullable = false)
    private String accountHolderEmail;

    private String accountHolderPhone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Region region;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DelinquencyBucket delinquencyBucket;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CollectionStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal outstandingBalance;

    @Column(nullable = false)
    private Integer daysPastDue;

    @Column(nullable = false)
    private LocalDate lastPaymentDate;

    private String assignedAgentId;

    private LocalDateTime assignedAt;

    @OneToMany(mappedBy = "delinquentAccount", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<CollectionAction> actions;

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

    public void addAction(CollectionAction action) {
        if (actions == null) {
            actions = new ArrayList<>();
        }
        if (action != null) {
            actions.add(action);
            action.setDelinquentAccount(this);
        }
    }
}
