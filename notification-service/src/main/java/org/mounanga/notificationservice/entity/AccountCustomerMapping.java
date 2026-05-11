package org.mounanga.notificationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AccountCustomerMapping {

    @Id
    private String accountId;

    @Column(nullable = false)
    private String customerId;
}
