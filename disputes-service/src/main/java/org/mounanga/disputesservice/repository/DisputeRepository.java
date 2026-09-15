package org.mounanga.disputesservice.repository;

import org.mounanga.disputesservice.entity.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisputeRepository extends JpaRepository<Dispute, String> {

    Page<Dispute> findByAccountIdOrderByCreatedDateDesc(String accountId, Pageable pageable);
}
