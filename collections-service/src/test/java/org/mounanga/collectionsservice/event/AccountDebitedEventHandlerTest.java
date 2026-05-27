package org.mounanga.collectionsservice.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mounanga.collectionsservice.common.enums.CollectionStatus;
import org.mounanga.collectionsservice.common.event.AccountDebitedEvent;
import org.mounanga.collectionsservice.common.event.CollectionInitiatedEvent;
import org.mounanga.collectionsservice.configuration.CollectionsProperties;
import org.mounanga.collectionsservice.entity.CollectionCase;
import org.mounanga.collectionsservice.repository.CollectionCaseRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "eureka.client.enabled=false",
        "axon.axonserver.enabled=false",
        "collections.overdraft-threshold=0"
})
class AccountDebitedEventHandlerTest {

    @Mock
    private CollectionCaseRepository collectionCaseRepository;

    @Mock
    private CollectionsProperties collectionsProperties;

    @Mock
    private CollectionEventPublisher collectionEventPublisher;

    @InjectMocks
    private AccountDebitedEventHandler accountDebitedEventHandler;

    @BeforeEach
    void setUp() {
        accountDebitedEventHandler = new AccountDebitedEventHandler(
                collectionCaseRepository, collectionsProperties, collectionEventPublisher
        );
    }

    @Test
    void testOnAccountDebitedEvent_exceedsThreshold_createsCollectionCase() {
        when(collectionsProperties.getOverdraftThreshold()).thenReturn(BigDecimal.valueOf(100));
        when(collectionCaseRepository.existsByAccountIdAndStatus(eq("account-1"), eq(CollectionStatus.FLAGGED))).thenReturn(false);

        CollectionCase savedCase = CollectionCase.builder()
                .id("case-1")
                .accountId("account-1")
                .overdraftAmount(BigDecimal.valueOf(500))
                .threshold(BigDecimal.valueOf(100))
                .status(CollectionStatus.FLAGGED)
                .flaggedAt(LocalDateTime.now())
                .build();
        when(collectionCaseRepository.save(any(CollectionCase.class))).thenReturn(savedCase);

        AccountDebitedEvent event = new AccountDebitedEvent(
                "account-1", LocalDateTime.now(), "system",
                BigDecimal.valueOf(500), "DEBIT", "Test debit"
        );

        accountDebitedEventHandler.on(event);

        verify(collectionCaseRepository, times(2)).save(any(CollectionCase.class));

        ArgumentCaptor<CollectionInitiatedEvent> captor = ArgumentCaptor.forClass(CollectionInitiatedEvent.class);
        verify(collectionEventPublisher).publish(captor.capture());

        CollectionInitiatedEvent publishedEvent = captor.getValue();
        assertEquals("case-1", publishedEvent.getCollectionId());
        assertEquals("account-1", publishedEvent.getAccountId());
        assertEquals(BigDecimal.valueOf(500), publishedEvent.getOverdraftAmount());
        assertEquals(BigDecimal.valueOf(100), publishedEvent.getThreshold());
    }

    @Test
    void testOnAccountDebitedEvent_belowThreshold_noCollectionCase() {
        when(collectionsProperties.getOverdraftThreshold()).thenReturn(BigDecimal.valueOf(1000));

        AccountDebitedEvent event = new AccountDebitedEvent(
                "account-1", LocalDateTime.now(), "system",
                BigDecimal.valueOf(500), "DEBIT", "Test debit"
        );

        accountDebitedEventHandler.on(event);

        verify(collectionCaseRepository, never()).save(any(CollectionCase.class));
        verify(collectionEventPublisher, never()).publish(any(CollectionInitiatedEvent.class));
    }

    @Test
    void testOnAccountDebitedEvent_alreadyFlagged_noNewCase() {
        when(collectionsProperties.getOverdraftThreshold()).thenReturn(BigDecimal.valueOf(100));
        when(collectionCaseRepository.existsByAccountIdAndStatus(eq("account-1"), eq(CollectionStatus.FLAGGED))).thenReturn(true);

        AccountDebitedEvent event = new AccountDebitedEvent(
                "account-1", LocalDateTime.now(), "system",
                BigDecimal.valueOf(500), "DEBIT", "Test debit"
        );

        accountDebitedEventHandler.on(event);

        verify(collectionCaseRepository, never()).save(any(CollectionCase.class));
        verify(collectionEventPublisher, never()).publish(any(CollectionInitiatedEvent.class));
    }

    @Test
    void testOnAccountDebitedEvent_equalToThreshold_noCollectionCase() {
        when(collectionsProperties.getOverdraftThreshold()).thenReturn(BigDecimal.valueOf(500));

        AccountDebitedEvent event = new AccountDebitedEvent(
                "account-1", LocalDateTime.now(), "system",
                BigDecimal.valueOf(500), "DEBIT", "Test debit"
        );

        accountDebitedEventHandler.on(event);

        verify(collectionCaseRepository, never()).save(any(CollectionCase.class));
        verify(collectionEventPublisher, never()).publish(any(CollectionInitiatedEvent.class));
    }
}
