package org.mounanga.collectionsservice.service.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mounanga.collectionsservice.dto.*;
import org.mounanga.collectionsservice.entity.CollectionAction;
import org.mounanga.collectionsservice.entity.DelinquentAccount;
import org.mounanga.collectionsservice.enums.*;
import org.mounanga.collectionsservice.event.CollectionEventPublisher;
import org.mounanga.collectionsservice.repository.CollectionActionRepository;
import org.mounanga.collectionsservice.repository.DelinquentAccountRepository;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionsServiceImplTest {

    @Mock
    private DelinquentAccountRepository delinquentAccountRepository;

    @Mock
    private CollectionActionRepository collectionActionRepository;

    @Mock
    private CollectionEventPublisher eventPublisher;

    @InjectMocks
    private CollectionsServiceImpl collectionsService;

    private DelinquentAccount sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = DelinquentAccount.builder()
                .id("da-001")
                .accountId("ACC-12345")
                .customerId("CUST-001")
                .customerName("John Doe")
                .email("john.doe@example.com")
                .outstandingBalance(new BigDecimal("5000.00"))
                .minimumPaymentDue(new BigDecimal("250.00"))
                .lastPaymentDate(LocalDate.now().minusDays(45))
                .dueDate(LocalDate.now().minusDays(35))
                .daysOverdue(35)
                .delinquencyBucket(DelinquencyBucket.DAYS_30)
                .productType(ProductType.CREDIT_CARD)
                .region(Region.EMEA)
                .collectionStatus(CollectionStatus.PENDING)
                .build();
    }

    @Test
    void getDelinquentAccounts_withFilters_shouldReturnFilteredResults() {
        Page<DelinquentAccount> page = new PageImpl<>(List.of(sampleAccount));
        when(delinquentAccountRepository.findByFilters(
                eq(DelinquencyBucket.DAYS_30), eq(ProductType.CREDIT_CARD),
                eq(Region.EMEA), isNull(), any(Pageable.class)))
                .thenReturn(page);

        DelinquentAccountPageResponseDTO result = collectionsService.getDelinquentAccounts(
                DelinquencyBucket.DAYS_30, ProductType.CREDIT_CARD, Region.EMEA, null, 0, 20);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getDelinquentAccounts().size());
        assertEquals("ACC-12345", result.getDelinquentAccounts().get(0).getAccountId());
    }

    @Test
    void getDelinquentAccounts_withNoFilters_shouldReturnAllResults() {
        Page<DelinquentAccount> page = new PageImpl<>(List.of(sampleAccount));
        when(delinquentAccountRepository.findByFilters(
                isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        DelinquentAccountPageResponseDTO result = collectionsService.getDelinquentAccounts(
                null, null, null, null, 0, 20);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getDelinquentAccountById_existingId_shouldReturnAccount() {
        when(delinquentAccountRepository.findById("da-001")).thenReturn(Optional.of(sampleAccount));

        DelinquentAccountResponseDTO result = collectionsService.getDelinquentAccountById("da-001");

        assertNotNull(result);
        assertEquals("da-001", result.getId());
        assertEquals("ACC-12345", result.getAccountId());
        assertEquals("CUST-001", result.getCustomerId());
        assertEquals(DelinquencyBucket.DAYS_30, result.getDelinquencyBucket());
    }

    @Test
    void getDelinquentAccountById_nonExistingId_shouldThrowException() {
        when(delinquentAccountRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(DelinquentAccountNotFoundException.class,
                () -> collectionsService.getDelinquentAccountById("nonexistent"));
    }

    @Test
    void getDelinquentAccountByAccountId_existingAccountId_shouldReturnAccount() {
        when(delinquentAccountRepository.findByAccountId("ACC-12345")).thenReturn(Optional.of(sampleAccount));

        DelinquentAccountResponseDTO result = collectionsService.getDelinquentAccountByAccountId("ACC-12345");

        assertNotNull(result);
        assertEquals("ACC-12345", result.getAccountId());
    }

    @Test
    void getDelinquentAccountsByCustomerId_shouldReturnAccounts() {
        when(delinquentAccountRepository.findByCustomerId("CUST-001")).thenReturn(List.of(sampleAccount));

        List<DelinquentAccountResponseDTO> result = collectionsService.getDelinquentAccountsByCustomerId("CUST-001");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CUST-001", result.get(0).getCustomerId());
    }

    @Test
    void searchDelinquentAccounts_shouldReturnMatchingResults() {
        Page<DelinquentAccount> page = new PageImpl<>(List.of(sampleAccount));
        when(delinquentAccountRepository.search(eq("%John%"), any(Pageable.class))).thenReturn(page);

        DelinquentAccountPageResponseDTO result = collectionsService.searchDelinquentAccounts("John", 0, 20);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getDelinquencySummary_shouldReturnBucketSummaries() {
        List<Object[]> summaryData = List.of(
                new Object[]{DelinquencyBucket.DAYS_30, 10L, new BigDecimal("50000.00")},
                new Object[]{DelinquencyBucket.DAYS_60, 5L, new BigDecimal("35000.00")},
                new Object[]{DelinquencyBucket.DAYS_90, 3L, new BigDecimal("25000.00")}
        );
        when(delinquentAccountRepository.getDelinquencySummary()).thenReturn(summaryData);

        List<DelinquencySummaryDTO> result = collectionsService.getDelinquencySummary();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(DelinquencyBucket.DAYS_30, result.get(0).getBucket());
        assertEquals(10L, result.get(0).getAccountCount());
        assertEquals(new BigDecimal("50000.00"), result.get(0).getTotalOutstandingBalance());
    }

    @Test
    void updateCollectionStatus_shouldUpdateAndPublishEvent() {
        when(delinquentAccountRepository.findById("da-001")).thenReturn(Optional.of(sampleAccount));
        when(delinquentAccountRepository.save(any(DelinquentAccount.class))).thenReturn(sampleAccount);

        DelinquentAccountResponseDTO result = collectionsService.updateCollectionStatus("da-001", CollectionStatus.IN_PROGRESS);

        assertNotNull(result);
        verify(delinquentAccountRepository).save(any(DelinquentAccount.class));
        verify(eventPublisher).publishStatusChanged(any(DelinquentAccount.class), eq(CollectionStatus.PENDING));
    }

    @Test
    void assignAgent_shouldAssignAndUpdateStatus() {
        when(delinquentAccountRepository.findById("da-001")).thenReturn(Optional.of(sampleAccount));
        when(delinquentAccountRepository.save(any(DelinquentAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        DelinquentAccountResponseDTO result = collectionsService.assignAgent("da-001", "AGENT-001");

        assertNotNull(result);
        assertEquals("AGENT-001", result.getAssignedAgentId());
        assertEquals(CollectionStatus.IN_PROGRESS, result.getCollectionStatus());
        verify(eventPublisher).publishAgentAssigned(any(DelinquentAccount.class), eq("AGENT-001"));
    }

    @Test
    void addCollectionAction_shouldSaveActionAndPublishEvent() {
        sampleAccount.setAssignedAgentId("AGENT-001");
        when(delinquentAccountRepository.findById("da-001")).thenReturn(Optional.of(sampleAccount));

        CollectionAction savedAction = CollectionAction.builder()
                .id("action-001")
                .actionType("PHONE_CALL")
                .description("Called customer regarding overdue payment")
                .performedBy("AGENT-001")
                .actionDate(LocalDateTime.now())
                .outcome("Left voicemail")
                .delinquentAccount(sampleAccount)
                .build();
        when(collectionActionRepository.save(any(CollectionAction.class))).thenReturn(savedAction);

        CollectionActionRequestDTO request = CollectionActionRequestDTO.builder()
                .actionType("PHONE_CALL")
                .description("Called customer regarding overdue payment")
                .actionDate(LocalDateTime.now())
                .outcome("Left voicemail")
                .build();

        CollectionActionResponseDTO result = collectionsService.addCollectionAction("da-001", request);

        assertNotNull(result);
        assertEquals("action-001", result.getId());
        assertEquals("PHONE_CALL", result.getActionType());
        verify(eventPublisher).publishActionRecorded(any(DelinquentAccount.class), any(CollectionAction.class));
    }

    @Test
    void getCollectionActions_shouldReturnActions() {
        CollectionAction action = CollectionAction.builder()
                .id("action-001")
                .actionType("PHONE_CALL")
                .description("Called customer")
                .performedBy("AGENT-001")
                .actionDate(LocalDateTime.now())
                .delinquentAccount(sampleAccount)
                .build();
        Page<CollectionAction> page = new PageImpl<>(List.of(action));
        when(collectionActionRepository.findByDelinquentAccountId(eq("da-001"), any(Pageable.class))).thenReturn(page);

        List<CollectionActionResponseDTO> result = collectionsService.getCollectionActions("da-001", 0, 20);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PHONE_CALL", result.get(0).getActionType());
    }
}
