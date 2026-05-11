package org.mounanga.notificationservice.repository;

import org.mounanga.notificationservice.entity.AccountCustomerMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountCustomerMappingRepository extends JpaRepository<AccountCustomerMapping, String> {
}
