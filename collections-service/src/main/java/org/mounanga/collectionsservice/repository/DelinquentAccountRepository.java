package org.mounanga.collectionsservice.repository;

import org.mounanga.collectionsservice.entity.DelinquentAccount;
import org.mounanga.collectionsservice.enums.CollectionStatus;
import org.mounanga.collectionsservice.enums.DelinquencyBucket;
import org.mounanga.collectionsservice.enums.ProductType;
import org.mounanga.collectionsservice.enums.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DelinquentAccountRepository extends JpaRepository<DelinquentAccount, String> {

    Optional<DelinquentAccount> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    @Query("""
            select d from DelinquentAccount d
            where (:bucket is null or d.delinquencyBucket = :bucket)
              and (:productType is null or d.productType = :productType)
              and (:region is null or d.region = :region)
              and (:status is null or d.status = :status)
            order by d.outstandingBalance desc
            """)
    Page<DelinquentAccount> searchDelinquentAccounts(@Param("bucket") DelinquencyBucket bucket,
                                                    @Param("productType") ProductType productType,
                                                    @Param("region") Region region,
                                                    @Param("status") CollectionStatus status,
                                                    Pageable pageable);

    @Query("""
            select d.delinquencyBucket as bucket, count(d) as accountCount, coalesce(sum(d.outstandingBalance), 0) as totalBalance
            from DelinquentAccount d
            group by d.delinquencyBucket
            """)
    List<DelinquencySummaryProjection> aggregateByBucket();

    interface DelinquencySummaryProjection {
        DelinquencyBucket getBucket();
        Long getAccountCount();
        java.math.BigDecimal getTotalBalance();
    }
}
