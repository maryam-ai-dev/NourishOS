package com.nourishos.authority.service.audit;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.*;
import com.nourishos.authority.repository.AuditRecordRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraceHelpersTest {

    @Mock private AuditRecordRepository auditRecordRepository;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks private DecisionTrace decisionTrace;
    @InjectMocks private ExecutionTrace executionTrace;
    @InjectMocks private ReplenishmentTrace replenishmentTrace;
    @InjectMocks private FoodFlowTrace foodFlowTrace;

    @Test
    void decisionTraceWritesPolicyDecision() {
        when(auditRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        PolicyDecision pd = new PolicyDecision();
        pd.setId(UUID.randomUUID());
        pd.setRuleType("REORDER");
        pd.setDecision("BLOCK");
        pd.setReason("Over limit");

        AuditRecord result = decisionTrace.write(pd);

        assertEquals("POLICY_DECISION", result.getEventType());
        assertEquals("SYSTEM", result.getActorType());
        assertTrue(result.getPayload().contains("REORDER"));
        assertTrue(result.getPayload().contains("BLOCK"));
        verify(auditRecordRepository).save(any());
    }

    @Test
    void executionTraceWritesStep() {
        when(auditRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        ExecutionStep step = new ExecutionStep();
        step.setId(UUID.randomUUID());
        step.setActionType("DISPENSE_DRY");
        step.setAssignedTo("MACHINE");

        AuditRecord result = executionTrace.write(step, "COMPLETE");

        assertEquals("EXECUTION_STEP", result.getEventType());
        assertTrue(result.getPayload().contains("DISPENSE_DRY"));
        assertTrue(result.getPayload().contains("COMPLETE"));
        verify(auditRecordRepository).save(any());
    }

    @Test
    void replenishmentTraceWritesSuggestion() {
        when(auditRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        ReplenishmentSuggestion s = new ReplenishmentSuggestion();
        s.setId(UUID.randomUUID());
        s.setAdjustedForWaste(true);
        s.setReason("Waste history");

        AuditRecord result = replenishmentTrace.write(s, "APPROVED");

        assertEquals("REPLENISHMENT_DECISION", result.getEventType());
        assertTrue(result.getPayload().contains("true"));
        assertTrue(result.getPayload().contains("APPROVED"));
        verify(auditRecordRepository).save(any());
    }

    @Test
    void foodFlowTraceWritesConsumption() {
        when(auditRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        ConsumptionEvent ce = new ConsumptionEvent();
        ce.setId(UUID.randomUUID());
        ce.setSource(ConsumptionSource.PLANNED_MEAL);
        ce.setQuantity(new BigDecimal("200"));
        ce.setUnit("g");

        AuditRecord result = foodFlowTrace.write(ce);

        assertEquals("CONSUMPTION", result.getEventType());
        verify(auditRecordRepository).save(any());
    }

    @Test
    void foodFlowTraceWritesWaste() {
        when(auditRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        WasteEvent we = new WasteEvent();
        we.setId(UUID.randomUUID());
        we.setWasteReason(WasteReason.EXPIRED);
        we.setQuantity(new BigDecimal("50"));
        we.setUnit("g");

        AuditRecord result = foodFlowTrace.write(we);

        assertEquals("WASTE", result.getEventType());
        verify(auditRecordRepository).save(any());
    }

    @Test
    void foodFlowTraceWritesMealOutcome() {
        when(auditRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        MealOutcomeEvent moe = new MealOutcomeEvent();
        moe.setId(UUID.randomUUID());
        moe.setOutcome(MealOutcome.ABANDONED);
        moe.setNotes("Too busy");

        AuditRecord result = foodFlowTrace.write(moe);

        assertEquals("MEAL_OUTCOME", result.getEventType());
        assertTrue(result.getPayload().contains("ABANDONED"));
        verify(auditRecordRepository).save(any());
    }
}
