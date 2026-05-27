package org.mounanga.collectionsservice.event;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.mounanga.collectionsservice.common.enums.CollectionStatus;
import org.mounanga.collectionsservice.common.event.AccountDebitedEvent;
import org.mounanga.collectionsservice.common.event.CollectionInitiatedEvent;
import org.mounanga.collectionsservice.configuration.CollectionsProperties;
import org.mounanga.collectionsservice.entity.CollectionCase;
import org.mounanga.collectionsservice.repository.CollectionCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
@Transactional
public class AccountDebitedEventHandler {

    private final CollectionCaseRepository collectionCaseRepository;
    private final CollectionsProperties collectionsProperties;
    private final CollectionEventPublisher collectionEventPublisher;

    public AccountDebitedEventHandler(CollectionCaseRepository collectionCaseRepository,
                                      CollectionsProperties collectionsProperties,
                                      CollectionEventPublisher collectionEventPublisher) {
        this.collectionCaseRepository = collectionCaseRepository;
        this.collectionsProperties = collectionsProperties;
        this.collectionEventPublisher = collectionEventPublisher;
    }

    @EventHandler
    public void on(AccountDebitedEvent event) {
        log.info("AccountDebitedEvent received for account '{}'", event.getId());

        BigDecimal threshold = collectionsProperties.getOverdraftThreshold();
        BigDecimal debitAmount = event.getAmount();

        if (debitAmount.compareTo(threshold) <= 0) {
            log.debug("Debit amount {} does not exceed overdraft threshold {}", debitAmount, threshold);
            return;
        }

        if (collectionCaseRepository.existsByAccountIdAndStatus(event.getId(), CollectionStatus.FLAGGED)) {
            log.info("Account '{}' already has an open FLAGGED collection case", event.getId());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        CollectionCase collectionCase = CollectionCase.builder()
                .accountId(event.getId())
                .overdraftAmount(debitAmount)
                .threshold(threshold)
                .status(CollectionStatus.FLAGGED)
                .flaggedAt(now)
                .build();

        CollectionCase saved = collectionCaseRepository.save(collectionCase);
        log.info("Collection case '{}' created for account '{}'", saved.getId(), saved.getAccountId());

        saved.setStatus(CollectionStatus.COLLECTION_INITIATED);
        collectionCaseRepository.save(saved);

        CollectionInitiatedEvent collectionEvent = new CollectionInitiatedEvent(
                saved.getId(),
                saved.getAccountId(),
                saved.getOverdraftAmount(),
                saved.getThreshold(),
                now
        );
        collectionEventPublisher.publish(collectionEvent);
    }
}
