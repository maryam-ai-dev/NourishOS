package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nourishos.authority.domain.Household;
import com.nourishos.authority.domain.Ingredient;
import com.nourishos.authority.domain.IngredientUsageRecord;
import com.nourishos.authority.repository.ConsumptionEventRepository;
import com.nourishos.authority.repository.IngredientUsageRecordRepository;
import com.nourishos.authority.repository.WasteEventRepository;
import com.nourishos.authority.service.HouseholdService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsageRecordService {

    private static final int WEEKS = 4;

    private final IngredientUsageRecordRepository usageRecordRepository;
    private final ConsumptionEventRepository consumptionEventRepository;
    private final WasteEventRepository wasteEventRepository;
    private final HouseholdService householdService;
    private final IngredientService ingredientService;

    @Transactional
    public IngredientUsageRecord recompute(UUID householdId, UUID ingredientId) {
        Household household = householdService.findById(householdId);
        Ingredient ingredient = ingredientService.findById(ingredientId);

        Instant cutoff = Instant.now().minus(WEEKS * 7, ChronoUnit.DAYS);

        BigDecimal totalConsumed = consumptionEventRepository.findByIngredientId(ingredientId).stream()
                .filter(e -> e.getHousehold().getId().equals(householdId))
                .filter(e -> e.getConsumedAt().isAfter(cutoff))
                .map(e -> e.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWasted = wasteEventRepository.findByIngredientIdAndWastedAtAfter(ingredientId, cutoff).stream()
                .filter(e -> e.getHousehold().getId().equals(householdId))
                .map(e -> e.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgWeekly = totalConsumed.divide(BigDecimal.valueOf(WEEKS), 4, RoundingMode.HALF_UP);

        IngredientUsageRecord record = usageRecordRepository
                .findByHouseholdIdAndIngredientId(householdId, ingredientId)
                .orElseGet(() -> {
                    IngredientUsageRecord r = new IngredientUsageRecord();
                    r.setHousehold(household);
                    r.setIngredient(ingredient);
                    return r;
                });

        record.setTotalConsumedLast4Weeks(totalConsumed);
        record.setTotalWastedLast4Weeks(totalWasted);
        record.setAvgWeeklyUsage(avgWeekly);

        return usageRecordRepository.save(record);
    }
}
