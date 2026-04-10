package com.nourishos.authority.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nourishos.authority.domain.HouseholdBudget;
import com.nourishos.authority.domain.WeeklySpendRecord;
import com.nourishos.authority.repository.HouseholdBudgetRepository;
import com.nourishos.authority.repository.WeeklySpendRecordRepository;
import com.nourishos.authority.service.HouseholdService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BudgetController {

    private final HouseholdBudgetRepository budgetRepository;
    private final WeeklySpendRecordRepository spendRepository;
    private final HouseholdService householdService;

    // --- Budget CRUD ---

    @PostMapping("/households/{id}/budget")
    @ResponseStatus(HttpStatus.CREATED)
    public HouseholdBudget createBudget(@PathVariable UUID id, @RequestBody HouseholdBudget budget) {
        householdService.findById(id);
        validateBudgetLimits(budget);
        budget.setHouseholdId(id);
        return budgetRepository.save(budget);
    }

    @GetMapping("/households/{id}/budget")
    public HouseholdBudget getBudget(@PathVariable UUID id) {
        return budgetRepository.findByHouseholdId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
    }

    @PutMapping("/households/{id}/budget")
    public HouseholdBudget updateBudget(@PathVariable UUID id, @RequestBody HouseholdBudget budget) {
        HouseholdBudget existing = budgetRepository.findByHouseholdId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
        validateBudgetLimits(budget);
        existing.setWeeklyLimitGbp(budget.getWeeklyLimitGbp());
        existing.setGroceriesLimitGbp(budget.getGroceriesLimitGbp());
        existing.setPantryLimitGbp(budget.getPantryLimitGbp());
        existing.setOtherLimitGbp(budget.getOtherLimitGbp());
        return budgetRepository.save(existing);
    }

    // --- Budget Status ---

    @GetMapping("/budget/status/{householdId}")
    public Map<String, Object> budgetStatus(@PathVariable UUID householdId) {
        householdService.findById(householdId);

        HouseholdBudget budget = budgetRepository.findByHouseholdId(householdId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));

        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        WeeklySpendRecord spend = spendRepository.findByHouseholdIdAndWeekStartDate(householdId, weekStart)
                .orElseGet(() -> {
                    WeeklySpendRecord empty = new WeeklySpendRecord();
                    empty.setHouseholdId(householdId);
                    empty.setWeekStartDate(weekStart);
                    return empty;
                });

        BigDecimal weeklyLimit = budget.getWeeklyLimitGbp() != null ? budget.getWeeklyLimitGbp() : BigDecimal.ZERO;
        BigDecimal totalSpent = spend.getTotalSpentGbp();
        BigDecimal remaining = weeklyLimit.subtract(totalSpent).max(BigDecimal.ZERO);
        BigDecimal spentPercent = weeklyLimit.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.divide(weeklyLimit, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return Map.of(
                "weeklyLimitGbp", weeklyLimit,
                "totalSpentGbp", totalSpent,
                "remainingGbp", remaining,
                "spentPercent", spentPercent,
                "categories", List.of(
                        categoryStatus("groceries", budget.getGroceriesLimitGbp(), spend.getGroceriesSpentGbp()),
                        categoryStatus("pantry", budget.getPantryLimitGbp(), spend.getPantrySpentGbp()),
                        categoryStatus("other", budget.getOtherLimitGbp(), spend.getOtherSpentGbp())
                )
        );
    }

    private Map<String, Object> categoryStatus(String name, BigDecimal limit, BigDecimal spent) {
        BigDecimal l = limit != null ? limit : BigDecimal.ZERO;
        BigDecimal s = spent != null ? spent : BigDecimal.ZERO;
        BigDecimal pct = l.compareTo(BigDecimal.ZERO) > 0
                ? s.divide(l, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        return Map.of("name", name, "limitGbp", l, "spentGbp", s, "spentPercent", pct);
    }

    private void validateBudgetLimits(HouseholdBudget budget) {
        if (budget.getWeeklyLimitGbp() != null) {
            BigDecimal sum = (budget.getGroceriesLimitGbp() != null ? budget.getGroceriesLimitGbp() : BigDecimal.ZERO)
                    .add(budget.getPantryLimitGbp() != null ? budget.getPantryLimitGbp() : BigDecimal.ZERO)
                    .add(budget.getOtherLimitGbp() != null ? budget.getOtherLimitGbp() : BigDecimal.ZERO);
            if (sum.compareTo(budget.getWeeklyLimitGbp()) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Category limits exceed weekly limit");
            }
        }
    }
}
