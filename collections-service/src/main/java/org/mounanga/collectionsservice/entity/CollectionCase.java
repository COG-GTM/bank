package org.mounanga.collectionsservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.mounanga.collectionsservice.common.enums.CollectionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "collection_case")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CollectionCase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false)
    private BigDecimal overdraftAmount;

    @Column(nullable = false)
    private BigDecimal threshold;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CollectionStatus status;

    @Column(nullable = false)
    private LocalDateTime flaggedAt;

    private LocalDateTime resolvedAt;

    private String resolvedBy;
}
