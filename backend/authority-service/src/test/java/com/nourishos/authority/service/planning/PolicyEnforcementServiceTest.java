package com.nourishos.authority.service.planning;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.PolicyDecision;
import com.nourishos.authority.domain.PolicySet;
import com.nourishos.authority.repository.PolicyDecisionRepository;
import com.nourishos.authority.repository.PolicySetRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyEnforcementServiceTest {

    @Mock private PolicySetRepository policySetRepository;
    @Mock private PolicyDecisionRepository decisionRepository;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks private PolicyEnforcementService service;

    private UUID householdId;
    private PolicySet policySet;

    @BeforeEach
    void setUp() {
        householdId = UUID.randomUUID();
        policySet = new PolicySet();
        policySet.setAutoReorderLimit(new BigDecimal("50.00"));
        policySet.setSubstitutionApprovalRequired(true);
        policySet.setNightModeEnabled(false);
        policySet.setMaxAutonomousActions(5);

        when(policySetRepository.findByHouseholdId(householdId)).thenReturn(Optional.of(policySet));
        when(decisionRepository.save(any())).thenAnswer(i -> {
            PolicyDecision d = i.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });
    }

    @Test
    void reorderAboveLimitReturnsBlock() {
        PolicyDecision result = service.evaluate(householdId, "REORDER", Map.of("cost", "75.00"));
        assertEquals("BLOCK", result.getDecision());
        verify(decisionRepository).save(any(PolicyDecision.class));
    }

    @Test
    void reorderWithinLimitReturnsAllow() {
        PolicyDecision result = service.evaluate(householdId, "REORDER", Map.of("cost", "30.00"));
        assertEquals("ALLOW", result.getDecision());
        verify(decisionRepository).save(any(PolicyDecision.class));
    }

    @Test
    void substitutionProtectedIngredientReturnsRequireApproval() {
        PolicyDecision result = service.evaluate(householdId, "SUBSTITUTION",
                Map.of("protectedIngredient", "true"));
        assertEquals("REQUIRE_APPROVAL", result.getDecision());
        verify(decisionRepository).save(any(PolicyDecision.class));
    }

    @Test
    void nightModeBlocksAutonomousAction() {
        policySet.setNightModeEnabled(true);
        PolicyDecision result = service.evaluate(householdId, "AUTONOMOUS_ACTION", Map.of());
        assertEquals("BLOCK", result.getDecision());
        verify(decisionRepository).save(any(PolicyDecision.class));
    }

    @Test
    void evaluateAlwaysWritesPolicyDecision() {
        service.evaluate(householdId, "REORDER", Map.of("cost", "10"));
        service.evaluate(householdId, "SUBSTITUTION", Map.of());
        service.evaluate(householdId, "UNKNOWN_TYPE", Map.of());
        verify(decisionRepository, times(3)).save(any(PolicyDecision.class));
    }
}
