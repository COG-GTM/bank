package org.mounanga.collectionsservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mounanga.collectionsservice.dto.AgentAssignmentRequestDTO;
import org.mounanga.collectionsservice.dto.CollectionActionRequestDTO;
import org.mounanga.collectionsservice.dto.CollectionActionResponseDTO;
import org.mounanga.collectionsservice.dto.DelinquencySummaryDTO;
import org.mounanga.collectionsservice.dto.DelinquentAccountPageResponseDTO;
import org.mounanga.collectionsservice.dto.DelinquentAccountRequestDTO;
import org.mounanga.collectionsservice.dto.DelinquentAccountResponseDTO;
import org.mounanga.collectionsservice.dto.StatusUpdateRequestDTO;
import org.mounanga.collectionsservice.entity.CollectionAction;
import org.mounanga.collectionsservice.entity.DelinquentAccount;
import org.mounanga.collectionsservice.enums.ActionOutcome;
import org.mounanga.collectionsservice.enums.ActionType;
import org.mounanga.collectionsservice.enums.CollectionStatus;
import org.mounanga.collectionsservice.enums.DelinquencyBucket;
import org.mounanga.collectionsservice.enums.ProductType;
import org.mounanga.collectionsservice.enums.Region;
import org.mounanga.collectionsservice.event.AgentAssignedEvent;
import org.mounanga.collectionsservice.event.CollectionActionRecordedEvent;
import org.mounanga.collectionsservice.event.DelinquentAccountStatusChangedEvent;
import org.mounanga.collectionsservice.exception.DelinquentAccountNotFoundException;
import org.mounanga.collectionsservice.exception.FieldValidationException;
import org.mounanga.collectionsservice.repository.CollectionActionRepository;
import org.mounanga.collectionsservice.repository.DelinquentAccountRepository;
import org.mounanga.collectionsservice.service.implementation.CollectionsServiceImpl;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionsServiceImplTest {

    @Mock
    private DelinquentAccountRepository delinquentAccountRepository;

    @Mock
    private CollectionActionRepository collectionActionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CollectionsServiceImpl service;

    private DelinquentAccount existingAccount;

    @BeforeEach
    void setUp() {
        existingAccount = DelinquentAccount.builder()
                .id("acc-1")
                .accountNumber("AN-001")
                .customerId("cust-1")
                .accountHolderName("Jane Doe")
                .accountHolderEmail("jane@example.com")
                .productType(ProductType.CREDIT_CARD)
                .region(Region.EMEA)
                .delinquencyBucket(DelinquencyBucket.DAYS_60)
                .status(CollectionStatus.NEW)
                .outstandingBalance(new BigDecimal("1250.00"))
                .daysPastDue(62)
                .lastPaymentDate(LocalDate.now().minusDays(62))
                .build();
    }

    @Test
    @DisplayName("getDelinquentAccountById returns mapped DTO when account exists")
    void getDelinquentAccountById_found() {
        when(delinquentAccountRepository.findById("acc-1")).thenReturn(Optional.of(existingAccount));

        DelinquentAccountResponseDTO result = service.getDelinquentAccountById("acc-1");

        assertThat(result.getId()).isEqualTo("acc-1");
        assertThat(result.getAccountNumber()).isEqualTo("AN-001");
        assertThat(result.getStatus()).isEqualTo(CollectionStatus.NEW);
    }

    @Test
    @DisplayName("getDelinquentAccountById throws when account is missing")
    void getDelinquentAccountById_notFound() {
        when(delinquentAccountRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDelinquentAccountById("missing"))
                .isInstanceOf(DelinquentAccountNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    @DisplayName("searchDelinquentAccounts passes filters through to the repository and returns a page DTO")
    void searchDelinquentAccounts_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DelinquentAccount> page = new PageImpl<>(List.of(existingAccount), pageable, 1);
        when(delinquentAccountRepository.searchDelinquentAccounts(
                DelinquencyBucket.DAYS_60, ProductType.CREDIT_CARD, Region.EMEA, null, pageable))
                .thenReturn(page);

        DelinquentAccountPageResponseDTO result = service.searchDelinquentAccounts(
                DelinquencyBucket.DAYS_60, ProductType.CREDIT_CARD, Region.EMEA, null, 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getDelinquentAccounts()).hasSize(1);
        assertThat(result.getDelinquentAccounts().get(0).getAccountNumber()).isEqualTo("AN-001");
    }

    @Test
    @DisplayName("createDelinquentAccount persists a new record with status NEW")
    void createDelinquentAccount_success() {
        DelinquentAccountRequestDTO dto = DelinquentAccountRequestDTO.builder()
                .accountNumber("AN-002")
                .customerId("cust-2")
                .accountHolderName("John Doe")
                .accountHolderEmail("john@example.com")
                .productType(ProductType.PERSONAL_LOAN)
                .region(Region.APAC)
                .delinquencyBucket(DelinquencyBucket.DAYS_30)
                .outstandingBalance(new BigDecimal("500.00"))
                .daysPastDue(31)
                .lastPaymentDate(LocalDate.now().minusDays(31))
                .build();
        when(delinquentAccountRepository.existsByAccountNumber("AN-002")).thenReturn(false);
        when(delinquentAccountRepository.save(any(DelinquentAccount.class)))
                .thenAnswer(invocation -> {
                    DelinquentAccount toSave = invocation.getArgument(0);
                    toSave.setId("acc-2");
                    return toSave;
                });

        DelinquentAccountResponseDTO result = service.createDelinquentAccount(dto);

        assertThat(result.getId()).isEqualTo("acc-2");
        assertThat(result.getStatus()).isEqualTo(CollectionStatus.NEW);
        assertThat(result.getAccountNumber()).isEqualTo("AN-002");
    }

    @Test
    @DisplayName("createDelinquentAccount rejects duplicate account numbers")
    void createDelinquentAccount_duplicate() {
        DelinquentAccountRequestDTO dto = DelinquentAccountRequestDTO.builder()
                .accountNumber("AN-001")
                .customerId("cust-1")
                .accountHolderName("Jane Doe")
                .accountHolderEmail("jane@example.com")
                .productType(ProductType.CREDIT_CARD)
                .region(Region.EMEA)
                .delinquencyBucket(DelinquencyBucket.DAYS_60)
                .outstandingBalance(BigDecimal.TEN)
                .daysPastDue(62)
                .lastPaymentDate(LocalDate.now().minusDays(62))
                .build();
        when(delinquentAccountRepository.existsByAccountNumber("AN-001")).thenReturn(true);

        assertThatThrownBy(() -> service.createDelinquentAccount(dto))
                .isInstanceOf(FieldValidationException.class);
        verify(delinquentAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus publishes a status change event when status changes")
    void updateStatus_publishesEvent() {
        when(delinquentAccountRepository.findById("acc-1")).thenReturn(Optional.of(existingAccount));
        when(delinquentAccountRepository.save(any(DelinquentAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        StatusUpdateRequestDTO update = StatusUpdateRequestDTO.builder()
                .status(CollectionStatus.ESCALATED)
                .reason("30+ days past due with no contact")
                .build();

        DelinquentAccountResponseDTO result = service.updateStatus("acc-1", update);

        assertThat(result.getStatus()).isEqualTo(CollectionStatus.ESCALATED);
        ArgumentCaptor<DelinquentAccountStatusChangedEvent> captor =
                ArgumentCaptor.forClass(DelinquentAccountStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        DelinquentAccountStatusChangedEvent event = captor.getValue();
        assertThat(event.getPreviousStatus()).isEqualTo(CollectionStatus.NEW);
        assertThat(event.getNewStatus()).isEqualTo(CollectionStatus.ESCALATED);
        assertThat(event.getReason()).contains("30+ days");
    }

    @Test
    @DisplayName("updateStatus does not publish an event when the status is unchanged")
    void updateStatus_noChangeNoEvent() {
        when(delinquentAccountRepository.findById("acc-1")).thenReturn(Optional.of(existingAccount));
        StatusUpdateRequestDTO update = StatusUpdateRequestDTO.builder()
                .status(CollectionStatus.NEW)
                .build();

        DelinquentAccountResponseDTO result = service.updateStatus("acc-1", update);

        assertThat(result.getStatus()).isEqualTo(CollectionStatus.NEW);
        verify(eventPublisher, never()).publishEvent(any());
        verify(delinquentAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("assignAgent sets the agent, auto-transitions NEW to IN_PROGRESS and fires two events")
    void assignAgent_transitionsFromNew() {
        when(delinquentAccountRepository.findById("acc-1")).thenReturn(Optional.of(existingAccount));
        when(delinquentAccountRepository.save(any(DelinquentAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        AgentAssignmentRequestDTO assignment = AgentAssignmentRequestDTO.builder()
                .agentId("agent-42")
                .build();

        DelinquentAccountResponseDTO result = service.assignAgent("acc-1", assignment);

        assertThat(result.getAssignedAgentId()).isEqualTo("agent-42");
        assertThat(result.getStatus()).isEqualTo(CollectionStatus.IN_PROGRESS);
        verify(eventPublisher).publishEvent(any(AgentAssignedEvent.class));
        verify(eventPublisher).publishEvent(any(DelinquentAccountStatusChangedEvent.class));
    }

    @Test
    @DisplayName("assignAgent keeps the status unchanged when it is not NEW and fires only the assignment event")
    void assignAgent_keepsStatusWhenNotNew() {
        existingAccount.setStatus(CollectionStatus.ESCALATED);
        when(delinquentAccountRepository.findById("acc-1")).thenReturn(Optional.of(existingAccount));
        when(delinquentAccountRepository.save(any(DelinquentAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        AgentAssignmentRequestDTO assignment = AgentAssignmentRequestDTO.builder()
                .agentId("agent-42")
                .build();

        DelinquentAccountResponseDTO result = service.assignAgent("acc-1", assignment);

        assertThat(result.getStatus()).isEqualTo(CollectionStatus.ESCALATED);
        verify(eventPublisher, times(1)).publishEvent(any(AgentAssignedEvent.class));
        verify(eventPublisher, never()).publishEvent(any(DelinquentAccountStatusChangedEvent.class));
    }

    @Test
    @DisplayName("recordAction persists a collection action and publishes a CollectionActionRecordedEvent")
    void recordAction_persistsAndPublishes() {
        when(delinquentAccountRepository.findById("acc-1")).thenReturn(Optional.of(existingAccount));
        when(collectionActionRepository.save(any(CollectionAction.class)))
                .thenAnswer(invocation -> {
                    CollectionAction action = invocation.getArgument(0);
                    action.setId("action-1");
                    if (action.getPerformedAt() == null) {
                        action.setPerformedAt(LocalDateTime.now());
                    }
                    return action;
                });
        CollectionActionRequestDTO request = CollectionActionRequestDTO.builder()
                .type(ActionType.PHONE_CALL)
                .outcome(ActionOutcome.LEFT_MESSAGE)
                .notes("Left a voicemail about the outstanding balance")
                .performedByAgentId("agent-42")
                .build();

        CollectionActionResponseDTO result = service.recordAction("acc-1", request);

        assertThat(result.getId()).isEqualTo("action-1");
        assertThat(result.getType()).isEqualTo(ActionType.PHONE_CALL);
        assertThat(result.getOutcome()).isEqualTo(ActionOutcome.LEFT_MESSAGE);
        verify(eventPublisher).publishEvent(any(CollectionActionRecordedEvent.class));
    }

    @Test
    @DisplayName("getActions returns the actions for the account ordered by time")
    void getActions_returnsList() {
        when(delinquentAccountRepository.findById("acc-1")).thenReturn(Optional.of(existingAccount));
        CollectionAction action = CollectionAction.builder()
                .id("action-1")
                .type(ActionType.EMAIL)
                .outcome(ActionOutcome.CONTACTED)
                .notes("Reached out via email")
                .performedByAgentId("agent-42")
                .performedAt(LocalDateTime.now())
                .delinquentAccount(existingAccount)
                .build();
        when(collectionActionRepository.findByDelinquentAccountIdOrderByPerformedAtDesc("acc-1"))
                .thenReturn(List.of(action));

        List<CollectionActionResponseDTO> result = service.getActions("acc-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDelinquentAccountId()).isEqualTo("acc-1");
    }

    @Test
    @DisplayName("getActions throws when the account does not exist")
    void getActions_missingAccount() {
        when(delinquentAccountRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getActions("missing"))
                .isInstanceOf(DelinquentAccountNotFoundException.class);
        verify(collectionActionRepository, never())
                .findByDelinquentAccountIdOrderByPerformedAtDesc(eq("missing"));
    }

    @Test
    @DisplayName("getDelinquencySummary fills in zero rows for buckets without any accounts")
    void getDelinquencySummary_zeroFill() {
        DelinquentAccountRepository.DelinquencySummaryProjection projection = new DelinquentAccountRepository.DelinquencySummaryProjection() {
            @Override
            public DelinquencyBucket getBucket() {
                return DelinquencyBucket.DAYS_30;
            }

            @Override
            public Long getAccountCount() {
                return 3L;
            }

            @Override
            public BigDecimal getTotalBalance() {
                return new BigDecimal("1500.00");
            }
        };
        when(delinquentAccountRepository.aggregateByBucket()).thenReturn(List.of(projection));

        List<DelinquencySummaryDTO> result = service.getDelinquencySummary();

        assertThat(result).hasSize(DelinquencyBucket.values().length);
        DelinquencySummaryDTO days30 = result.stream()
                .filter(r -> r.getBucket() == DelinquencyBucket.DAYS_30)
                .findFirst()
                .orElseThrow();
        assertThat(days30.getAccountCount()).isEqualTo(3L);
        assertThat(days30.getTotalOutstandingBalance()).isEqualByComparingTo("1500.00");
        DelinquencySummaryDTO days90 = result.stream()
                .filter(r -> r.getBucket() == DelinquencyBucket.DAYS_90)
                .findFirst()
                .orElseThrow();
        assertThat(days90.getAccountCount()).isZero();
        assertThat(days90.getTotalOutstandingBalance()).isEqualByComparingTo("0");
    }
}
