package org.mounanga.collectionsservice.repository;

import org.mounanga.collectionsservice.entity.CollectionAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionActionRepository extends JpaRepository<CollectionAction, String> {

    Page<CollectionAction> findByDelinquentAccountId(String delinquentAccountId, Pageable pageable);
}
