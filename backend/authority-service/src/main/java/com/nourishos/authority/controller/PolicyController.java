package com.nourishos.authority.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.PolicyDecision;
import com.nourishos.authority.domain.PolicySet;
import com.nourishos.authority.repository.PolicyDecisionRepository;
import com.nourishos.authority.repository.PolicySetRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicySetRepository policySetRepository;
    private final PolicyDecisionRepository decisionRepository;

    @GetMapping("/{householdId}")
    public PolicySet get(@PathVariable UUID householdId) {
        return policySetRepository.findByHouseholdId(householdId)
                .orElseThrow(() -> new IllegalArgumentException("PolicySet not found for household: " + householdId));
    }

    @PutMapping("/{householdId}")
    public PolicySet update(@PathVariable UUID householdId, @RequestBody PolicySet updated) {
        PolicySet existing = policySetRepository.findByHouseholdId(householdId)
                .orElseThrow(() -> new IllegalArgumentException("PolicySet not found for household: " + householdId));
        existing.setAutoReorderLimit(updated.getAutoReorderLimit());
        existing.setSubstitutionApprovalRequired(updated.isSubstitutionApprovalRequired());
        existing.setNightModeEnabled(updated.isNightModeEnabled());
        existing.setMaxAutonomousActions(updated.getMaxAutonomousActions());
        existing.setWasteAlertThreshold(updated.getWasteAlertThreshold());
        return policySetRepository.save(existing);
    }

    @GetMapping("/{householdId}/decisions")
    public List<PolicyDecision> decisions(@PathVariable UUID householdId) {
        return decisionRepository.findAll().stream()
                .sorted((a, b) -> b.getDecidedAt().compareTo(a.getDecidedAt()))
                .toList();
    }
}
