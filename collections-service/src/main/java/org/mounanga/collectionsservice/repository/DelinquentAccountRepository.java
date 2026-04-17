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

    Page<DelinquentAccount> findByDelinquencyBucket(DelinquencyBucket bucket, Pageable pageable);

    Page<DelinquentAccount> findByProductType(ProductType productType, Pageable pageable);

    Page<DelinquentAccount> findByRegion(Region region, Pageable pageable);

    Page<DelinquentAccount> findByCollectionStatus(CollectionStatus status, Pageable pageable);

    Optional<DelinquentAccount> findByAccountId(String accountId);

    List<DelinquentAccount> findByCustomerId(String customerId);

    @Query("SELECT d FROM DelinquentAccount d WHERE " +
            "(:bucket IS NULL OR d.delinquencyBucket = :bucket) AND " +
            "(:productType IS NULL OR d.productType = :productType) AND " +
            "(:region IS NULL OR d.region = :region) AND " +
            "(:status IS NULL OR d.collectionStatus = :status)")
    Page<DelinquentAccount> findByFilters(
            @Param("bucket") DelinquencyBucket bucket,
            @Param("productType") ProductType productType,
            @Param("region") Region region,
            @Param("status") CollectionStatus status,
            Pageable pageable);

    @Query("SELECT d.delinquencyBucket, COUNT(d), SUM(d.outstandingBalance) " +
            "FROM DelinquentAccount d " +
            "WHERE d.collectionStatus <> 'RESOLVED' AND d.collectionStatus <> 'WRITTEN_OFF' " +
            "GROUP BY d.delinquencyBucket " +
            "ORDER BY d.delinquencyBucket")
    List<Object[]> getDelinquencySummary();

    @Query("SELECT d FROM DelinquentAccount d WHERE d.daysOverdue >= :minDays ORDER BY d.outstandingBalance DESC")
    Page<DelinquentAccount> findByMinDaysOverdue(@Param("minDays") int minDays, Pageable pageable);

    @Query("SELECT d FROM DelinquentAccount d WHERE " +
            "d.customerName LIKE :keyword OR d.email LIKE :keyword OR d.accountId LIKE :keyword " +
            "ORDER BY d.daysOverdue DESC")
    Page<DelinquentAccount> search(@Param("keyword") String keyword, Pageable pageable);
}
