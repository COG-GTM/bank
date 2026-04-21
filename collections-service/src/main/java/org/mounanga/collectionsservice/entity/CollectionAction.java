package org.mounanga.collectionsservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.mounanga.collectionsservice.enums.ActionOutcome;
import org.mounanga.collectionsservice.enums.ActionType;

import java.time.LocalDateTime;

@Entity
@Table(name = "collection_action")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CollectionAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionOutcome outcome;

    @Column(nullable = false, length = 1000)
    private String notes;

    @Column(nullable = false)
    private String performedByAgentId;

    @Column(nullable = false)
    private LocalDateTime performedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delinquent_account_id", nullable = false)
    @ToString.Exclude
    private DelinquentAccount delinquentAccount;
}
