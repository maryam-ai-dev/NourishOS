package com.nourishos.authority.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.WeeklySavingsSnapshot;
import com.nourishos.authority.service.HouseholdService;
import com.nourishos.authority.service.savings.SavingsSnapshotService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/savings")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsSnapshotService savingsService;
    private final HouseholdService householdService;

    @GetMapping("/snapshot/{householdId}")
    public WeeklySavingsSnapshot getSnapshot(@PathVariable UUID householdId) {
        // Verify household exists (throws 404 if not)
        householdService.findById(householdId);

        // Generate or return existing snapshot for current week
        return savingsService.generate(householdId);
    }
}
