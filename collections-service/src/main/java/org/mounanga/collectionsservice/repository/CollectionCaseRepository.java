package org.mounanga.collectionsservice.repository;

import org.mounanga.collectionsservice.common.enums.CollectionStatus;
import org.mounanga.collectionsservice.entity.CollectionCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionCaseRepository extends JpaRepository<CollectionCase, String> {

    List<CollectionCase> findByAccountId(String accountId);

    List<CollectionCase> findByStatus(CollectionStatus status);

    Optional<CollectionCase> findByAccountIdAndStatus(String accountId, CollectionStatus status);

    boolean existsByAccountIdAndStatus(String accountId, CollectionStatus status);
}
