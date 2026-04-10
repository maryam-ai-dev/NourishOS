package com.nourishos.authority.controller;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.HouseholdBudget;
import com.nourishos.authority.domain.ReplenishmentRequest;
import com.nourishos.authority.domain.ReplenishmentSuggestion;
import com.nourishos.authority.domain.WeeklySpendRecord;
import com.nourishos.authority.dto.BundleRequestDto;
import com.nourishos.authority.dto.SuggestionResponse;
import com.nourishos.authority.repository.HouseholdBudgetRepository;
import com.nourishos.authority.repository.ReplenishmentSuggestionRepository;
import com.nourishos.authority.repository.WeeklySpendRecordRepository;
import com.nourishos.authority.service.planning.ReplenishmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/replenishment")
@RequiredArgsConstructor
public class ReplenishmentController {

    private static final Map<String, Integer> URGENCY_ORDER = Map.of(
            "CRITICAL", 0, "HIGH", 1, "MEDIUM", 2, "LOW", 3);

    private final ReplenishmentSuggestionRepository suggestionRepository;
    private final ReplenishmentService replenishmentService;
    private final HouseholdBudgetRepository budgetRepository;
    private final WeeklySpendRecordRepository spendRepository;

    @GetMapping("/suggestions")
    @Transactional(readOnly = true)
    public List<SuggestionResponse> suggestions(@RequestParam UUID householdId) {
        return suggestionRepository.findByHouseholdId(householdId).stream()
                .map(SuggestionResponse::from)
                .sorted(Comparator.comparingInt(s -> URGENCY_ORDER.getOrDefault(s.getUrgency(), 99)))
                .toList();
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ReplenishmentRequest bundleRequest(@Valid @RequestBody BundleRequestDto dto) {
        return replenishmentService.bundleRequest(dto);
    }

    @PostMapping("/requests/approve-all-within-budget/{householdId}")
    @Transactional
    public Map<String, Object> approveAllWithinBudget(@PathVariable UUID householdId) {
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        HouseholdBudget budget = budgetRepository.findByHouseholdId(householdId).orElse(null);
        BigDecimal remaining = budget != null && budget.getWeeklyLimitGbp() != null
                ? budget.getWeeklyLimitGbp() : new BigDecimal("999999");

        WeeklySpendRecord spend = spendRepository.findByHouseholdIdAndWeekStartDate(householdId, weekStart)
                .orElse(null);
        if (spend != null) {
            remaining = remaining.subtract(spend.getTotalSpentGbp()).max(BigDecimal.ZERO);
        }

        List<ReplenishmentSuggestion> pending = suggestionRepository.findByHouseholdId(householdId).stream()
                .filter(s -> "PENDING".equals(s.getStatus()))
                .toList();

        List<String> approved = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        BigDecimal runningCost = BigDecimal.ZERO;

        for (ReplenishmentSuggestion sug : pending) {
            BigDecimal cost = sug.getSuggestedQuantity() != null
                    ? sug.getSuggestedQuantity() : BigDecimal.ZERO;
            if (runningCost.add(cost).compareTo(remaining) <= 0) {
                sug.setStatus("APPROVED");
                suggestionRepository.save(sug);
                runningCost = runningCost.add(cost);
                approved.add(sug.getId().toString());
            } else {
                skipped.add(sug.getId().toString());
            }
        }

        return Map.of(
                "approved", approved,
                "skipped", skipped,
                "reason", skipped.isEmpty() ? "All within budget" : "Some exceeded remaining budget"
        );
    }
}
