package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nourishos.authority.domain.ConsumptionEvent;
import com.nourishos.authority.domain.Household;
import com.nourishos.authority.domain.Ingredient;
import com.nourishos.authority.domain.IngredientUsageRecord;
import com.nourishos.authority.domain.WasteEvent;
import com.nourishos.authority.repository.ConsumptionEventRepository;
import com.nourishos.authority.repository.IngredientUsageRecordRepository;
import com.nourishos.authority.repository.WasteEventRepository;
import com.nourishos.authority.service.HouseholdService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsageRecordServiceTest {

    @Mock private IngredientUsageRecordRepository usageRecordRepository;
    @Mock private ConsumptionEventRepository consumptionEventRepository;
    @Mock private WasteEventRepository wasteEventRepository;
    @Mock private HouseholdService householdService;
    @Mock private IngredientService ingredientService;

    @InjectMocks private UsageRecordService service;

    private UUID householdId;
    private UUID ingredientId;
    private Household household;
    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        householdId = UUID.randomUUID();
        ingredientId = UUID.randomUUID();
        household = new Household();
        household.setId(householdId);
        ingredient = new Ingredient();
        ingredient.setId(ingredientId);
    }

    private ConsumptionEvent consumption(BigDecimal qty) {
        ConsumptionEvent e = new ConsumptionEvent();
        e.setHousehold(household);
        e.setQuantity(qty);
        e.setConsumedAt(Instant.now());
        return e;
    }

    private WasteEvent waste(BigDecimal qty) {
        WasteEvent e = new WasteEvent();
        e.setHousehold(household);
        e.setQuantity(qty);
        e.setWastedAt(Instant.now());
        return e;
    }

    @Test
    void recomputeCalculatesCorrectTotals() {
        when(householdService.findById(householdId)).thenReturn(household);
        when(ingredientService.findById(ingredientId)).thenReturn(ingredient);
        when(consumptionEventRepository.findByIngredientId(ingredientId))
                .thenReturn(List.of(
                        consumption(new BigDecimal("100")),
                        consumption(new BigDecimal("200")),
                        consumption(new BigDecimal("50"))));
        when(wasteEventRepository.findByIngredientIdAndWastedAtAfter(eq(ingredientId), any()))
                .thenReturn(List.of(waste(new BigDecimal("50"))));
        when(usageRecordRepository.findByHouseholdIdAndIngredientId(householdId, ingredientId))
                .thenReturn(Optional.empty());
        when(usageRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        IngredientUsageRecord result = service.recompute(householdId, ingredientId);

        assertEquals(0, new BigDecimal("350").compareTo(result.getTotalConsumedLast4Weeks()));
        assertEquals(0, new BigDecimal("50").compareTo(result.getTotalWastedLast4Weeks()));
        assertEquals(0, new BigDecimal("87.5000").compareTo(result.getAvgWeeklyUsage()));
        verify(usageRecordRepository).save(any());
    }

    @Test
    void recomputeUpdatesExistingRecord() {
        IngredientUsageRecord existing = new IngredientUsageRecord();
        existing.setId(UUID.randomUUID());
        existing.setHousehold(household);
        existing.setIngredient(ingredient);

        when(householdService.findById(householdId)).thenReturn(household);
        when(ingredientService.findById(ingredientId)).thenReturn(ingredient);
        when(consumptionEventRepository.findByIngredientId(ingredientId)).thenReturn(List.of());
        when(wasteEventRepository.findByIngredientIdAndWastedAtAfter(eq(ingredientId), any()))
                .thenReturn(List.of());
        when(usageRecordRepository.findByHouseholdIdAndIngredientId(householdId, ingredientId))
                .thenReturn(Optional.of(existing));
        when(usageRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        IngredientUsageRecord result = service.recompute(householdId, ingredientId);

        assertEquals(existing.getId(), result.getId());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalConsumedLast4Weeks()));
    }
}
