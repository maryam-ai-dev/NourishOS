package com.nourishos.authority.service.savings;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nourishos.authority.domain.Ingredient;
import com.nourishos.authority.domain.MealOutcome;
import com.nourishos.authority.domain.MealOutcomeEvent;
import com.nourishos.authority.domain.WasteEvent;
import com.nourishos.authority.domain.WeeklySavingsSnapshot;
import com.nourishos.authority.repository.IngredientRepository;
import com.nourishos.authority.repository.MealOutcomeEventRepository;
import com.nourishos.authority.repository.WasteEventRepository;
import com.nourishos.authority.repository.WeeklySavingsSnapshotRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingsSnapshotService {

    private final WeeklySavingsSnapshotRepository snapshotRepository;
    private final WasteEventRepository wasteEventRepository;
    private final MealOutcomeEventRepository outcomeEventRepository;
    private final IngredientRepository ingredientRepository;

    public BigDecimal computeWasteSaved(UUID householdId, LocalDate weekStart) {
        Instant start = weekStart.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = weekStart.plusDays(7).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<WasteEvent> wasteEvents = wasteEventRepository.findByHouseholdIdAndWastedAtBetween(
                householdId, start, end);

        BigDecimal total = BigDecimal.ZERO;
        for (WasteEvent we : wasteEvents) {
            Ingredient ing = ingredientRepository.findById(we.getIngredient().getId()).orElse(null);
            if (ing != null && ing.getEstimatedCostPerUnit() != null) {
                total = total.add(we.getQuantity().multiply(ing.getEstimatedCostPerUnit()));
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public WeeklySavingsSnapshot generate(UUID householdId) {
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate prevWeekStart = weekStart.minusWeeks(1);

        BigDecimal thisWeekSaved = computeWasteSaved(householdId, weekStart);
        BigDecimal prevWeekSaved = computeWasteSaved(householdId, prevWeekStart);

        int wasteThisWeek = countWasteEvents(householdId, weekStart);
        int wastePrevWeek = countWasteEvents(householdId, prevWeekStart);

        BigDecimal completionRate = computeCompletionRate(householdId, weekStart);

        return snapshotRepository.findByHouseholdIdAndWeekStartDate(householdId, weekStart)
                .map(existing -> {
                    existing.setSavedFromWasteGbp(thisWeekSaved);
                    existing.setPreviousWeekSavedGbp(prevWeekSaved);
                    existing.setWasteItemsThisWeek(wasteThisWeek);
                    existing.setWasteItemsPreviousWeek(wastePrevWeek);
                    existing.setMealsCompletedRate(completionRate);
                    existing.setCreatedAt(Instant.now());
                    return snapshotRepository.save(existing);
                })
                .orElseGet(() -> {
                    WeeklySavingsSnapshot snap = new WeeklySavingsSnapshot();
                    snap.setHouseholdId(householdId);
                    snap.setWeekStartDate(weekStart);
                    snap.setSavedFromWasteGbp(thisWeekSaved);
                    snap.setPreviousWeekSavedGbp(prevWeekSaved);
                    snap.setWasteItemsThisWeek(wasteThisWeek);
                    snap.setWasteItemsPreviousWeek(wastePrevWeek);
                    snap.setMealsCompletedRate(completionRate);
                    return snapshotRepository.save(snap);
                });
    }

    private int countWasteEvents(UUID householdId, LocalDate weekStart) {
        Instant start = weekStart.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = weekStart.plusDays(7).atStartOfDay(ZoneOffset.UTC).toInstant();
        return wasteEventRepository.findByHouseholdIdAndWastedAtBetween(householdId, start, end).size();
    }

    private BigDecimal computeCompletionRate(UUID householdId, LocalDate weekStart) {
        Instant start = weekStart.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = weekStart.plusDays(7).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<MealOutcomeEvent> outcomes = outcomeEventRepository.findByHouseholdIdAndCompletedAtBetween(
                householdId, start, end);

        if (outcomes.isEmpty()) return BigDecimal.ZERO;

        long completed = outcomes.stream()
                .filter(o -> o.getOutcome() == MealOutcome.COMPLETED)
                .count();

        return BigDecimal.valueOf(completed)
                .divide(BigDecimal.valueOf(outcomes.size()), 4, RoundingMode.HALF_UP);
    }
}
