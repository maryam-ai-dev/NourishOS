package com.nourishos.authority.service.execution;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.AdjustmentType;
import com.nourishos.authority.domain.ConsumptionEvent;
import com.nourishos.authority.domain.ConsumptionSource;
import com.nourishos.authority.domain.ExecutionStep;
import com.nourishos.authority.domain.Household;
import com.nourishos.authority.domain.LotAllocation;
import com.nourishos.authority.domain.MealPlan;
import com.nourishos.authority.repository.ConsumptionEventRepository;
import com.nourishos.authority.service.HouseholdService;
import com.nourishos.authority.service.inventory.IngredientService;
import com.nourishos.authority.service.inventory.InventoryAdjustmentService;
import com.nourishos.authority.service.inventory.LotAllocationService;
import com.nourishos.authority.service.inventory.UsageRecordService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExecutionStepService {

    private final LotAllocationService lotAllocationService;
    private final InventoryAdjustmentService adjustmentService;
    private final ConsumptionEventRepository consumptionEventRepository;
    private final IngredientService ingredientService;
    private final UsageRecordService usageRecordService;
    private final HouseholdService householdService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handleDispenseStepComplete(ExecutionStep step, MealPlan mealPlan, UUID householdId) {
        if (step.getIngredientRef() == null) {
            return;
        }

        try {
            var ref = objectMapper.readValue(step.getIngredientRef(), new TypeReference<java.util.Map<String, Object>>() {});
            UUID ingredientId = UUID.fromString(ref.get("ingredientId").toString());
            BigDecimal quantity = new BigDecimal(ref.get("quantity").toString());
            String unit = ref.get("unit").toString();

            // Allocate lots (opened first, nearest expiry)
            List<LotAllocation> allocations = lotAllocationService.allocate(
                    ingredientId, householdId, quantity, unit);

            // Deduct from each lot
            for (LotAllocation alloc : allocations) {
                adjustmentService.record(
                        alloc.getLotId(),
                        AdjustmentType.DEDUCTION,
                        alloc.getQuantityFromThisLot().negate(),
                        alloc.getUnit(),
                        "Execution step: " + step.getActionType());
            }

            // Create consumption event
            Household household = householdService.findById(householdId);
            ConsumptionEvent event = new ConsumptionEvent();
            event.setHousehold(household);
            event.setIngredient(ingredientService.findById(ingredientId));
            event.setQuantity(quantity);
            event.setUnit(unit);
            event.setSource(ConsumptionSource.PLANNED_MEAL);
            event.setMealPlan(mealPlan);
            if (!allocations.isEmpty()) {
                // Link to first allocated lot
                event.setLot(new com.nourishos.authority.domain.IngredientLot());
                event.getLot().setId(allocations.get(0).getLotId());
            }
            consumptionEventRepository.save(event);

            // Recompute usage record
            usageRecordService.recompute(householdId, ingredientId);

        } catch (JsonProcessingException e) {
            // Invalid ref — skip
        }
    }
}
