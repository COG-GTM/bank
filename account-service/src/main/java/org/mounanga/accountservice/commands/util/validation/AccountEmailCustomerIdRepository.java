package org.mounanga.accountservice.commands.util.validation;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountEmailCustomerIdRepository extends JpaRepository<AccountEmailCustomerId, String> {

    @Override
    boolean existsById(@NotNull String s);
    boolean existsByCustomerId(String customerId);
    AccountEmailCustomerId findByCustomerId(String customerId);
}
