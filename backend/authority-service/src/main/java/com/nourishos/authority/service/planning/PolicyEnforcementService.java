package com.nourishos.authority.service.planning;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.PolicyDecision;
import com.nourishos.authority.domain.PolicySet;
import com.nourishos.authority.repository.PolicyDecisionRepository;
import com.nourishos.authority.repository.PolicySetRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyEnforcementService {

    private final PolicySetRepository policySetRepository;
    private final PolicyDecisionRepository decisionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public PolicyDecision evaluate(UUID householdId, String ruleType, Map<String, Object> context) {
        PolicySet policySet = policySetRepository.findByHouseholdId(householdId)
                .orElseThrow(() -> new IllegalArgumentException("PolicySet not found for household: " + householdId));

        String decision;
        String reason;

        switch (ruleType) {
            case "REORDER" -> {
                BigDecimal cost = new BigDecimal(context.getOrDefault("cost", "0").toString());
                if (cost.compareTo(policySet.getAutoReorderLimit()) > 0) {
                    decision = "BLOCK";
                    reason = "Cost " + cost + " exceeds auto-reorder limit " + policySet.getAutoReorderLimit();
                } else {
                    decision = "ALLOW";
                    reason = "Cost " + cost + " within auto-reorder limit";
                }
            }
            case "SUBSTITUTION" -> {
                boolean isProtected = Boolean.parseBoolean(
                        context.getOrDefault("protectedIngredient", "false").toString());
                if (isProtected && policySet.isSubstitutionApprovalRequired()) {
                    decision = "REQUIRE_APPROVAL";
                    reason = "Protected ingredient requires approval";
                } else {
                    decision = "ALLOW";
                    reason = "Substitution allowed";
                }
            }
            case "AUTONOMOUS_ACTION" -> {
                if (policySet.isNightModeEnabled()) {
                    decision = "BLOCK";
                    reason = "Night mode is active — autonomous actions blocked";
                } else {
                    decision = "ALLOW";
                    reason = "Autonomous action permitted";
                }
            }
            default -> {
                decision = "ALLOW";
                reason = "No specific rule for type: " + ruleType;
            }
        }

        PolicyDecision pd = new PolicyDecision();
        pd.setRuleType(ruleType);
        pd.setInput(toJson(context));
        pd.setDecision(decision);
        pd.setReason(reason);
        return decisionRepository.save(pd);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
