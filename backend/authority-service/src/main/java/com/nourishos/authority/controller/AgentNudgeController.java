package com.nourishos.authority.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.service.HouseholdService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AgentNudgeController {

    private final HouseholdService householdService;

    @GetMapping("/agent/nudge/{householdId}")
    public Map<String, Object> getNudge(@PathVariable UUID householdId) {
        // Verify household exists — returns generic nudge for empty household, not 404
        try {
            householdService.findById(householdId);
        } catch (Exception e) {
            return Map.of(
                "nudgeType", "WELCOME",
                "message", "Welcome to NourishOS! Start by adding your household members.",
                "savingsGbp", 0.0,
                "actionType", "NAVIGATE",
                "actionPayload", "/household"
            );
        }

        // Priority order: expiring-today > low-stock critical > waste pattern > budget threshold
        // In production, each check queries real data. For now, return a contextual nudge.
        return Map.of(
            "nudgeType", "GENERAL",
            "message", "Your pantry is looking good. Plan this week's meals to keep waste low.",
            "savingsGbp", 0.0,
            "actionType", "NAVIGATE",
            "actionPayload", "/planner"
        );
    }
}
