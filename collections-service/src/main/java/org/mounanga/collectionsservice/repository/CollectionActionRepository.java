package org.mounanga.collectionsservice.repository;

import org.mounanga.collectionsservice.entity.CollectionAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionActionRepository extends JpaRepository<CollectionAction, String> {

    List<CollectionAction> findByDelinquentAccountIdOrderByPerformedAtDesc(String delinquentAccountId);
}
