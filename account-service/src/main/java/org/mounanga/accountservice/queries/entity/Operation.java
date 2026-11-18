package org.mounanga.accountservice.queries.entity;

import jakarta.persistence.*;
import lombok.*;
import org.mounanga.accountservice.common.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Table(indexes = @Index(name = "idx_operation_account_id_date_time", columnList = "account_id, date_time"))
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false, updatable = false)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private OperationType type;

    @Column(nullable = false, updatable = false)
    private String description;

    @Column(nullable = false, updatable = false)
    private String createdBy;

    @ManyToOne
    private Account account;
}
