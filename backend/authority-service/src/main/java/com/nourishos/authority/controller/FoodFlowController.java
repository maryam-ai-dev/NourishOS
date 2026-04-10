package com.nourishos.authority.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.ConsumptionEvent;
import com.nourishos.authority.domain.FoodFlowSnapshot;
import com.nourishos.authority.domain.IngredientUsageRecord;
import com.nourishos.authority.domain.MealOutcomeEvent;
import com.nourishos.authority.domain.WasteEvent;
import com.nourishos.authority.dto.CreateConsumptionEventDto;
import com.nourishos.authority.dto.CreateMealOutcomeDto;
import com.nourishos.authority.dto.CreateWasteEventDto;
import com.nourishos.authority.repository.ConsumptionEventRepository;
import com.nourishos.authority.repository.IngredientLotRepository;
import com.nourishos.authority.repository.IngredientUsageRecordRepository;
import com.nourishos.authority.repository.MealOutcomeEventRepository;
import com.nourishos.authority.repository.MealPlanRepository;
import com.nourishos.authority.repository.WasteEventRepository;
import com.nourishos.authority.service.HouseholdService;
import com.nourishos.authority.service.inventory.FoodFlowSnapshotService;
import com.nourishos.authority.service.inventory.IngredientService;
import com.nourishos.authority.service.inventory.UsageRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/foodflow")
@RequiredArgsConstructor
public class FoodFlowController {

    private final FoodFlowSnapshotService snapshotService;
    private final ConsumptionEventRepository consumptionEventRepository;
    private final WasteEventRepository wasteEventRepository;
    private final UsageRecordService usageRecordService;
    private final HouseholdService householdService;
    private final IngredientService ingredientService;
    private final IngredientLotRepository lotRepository;
    private final MealOutcomeEventRepository mealOutcomeEventRepository;
    private final MealPlanRepository mealPlanRepository;
    private final IngredientUsageRecordRepository usageRecordRepository;

    @GetMapping("/snapshot")
    public FoodFlowSnapshot snapshot(@RequestParam UUID householdId) {
        return snapshotService.getLatest(householdId);
    }

    @GetMapping("/snapshot/refresh")
    public FoodFlowSnapshot refresh(@RequestParam UUID householdId) {
        return snapshotService.generate(householdId);
    }

    @PostMapping("/events/consumption")
    @ResponseStatus(HttpStatus.CREATED)
    public ConsumptionEvent recordConsumption(@Valid @RequestBody CreateConsumptionEventDto dto) {
        ConsumptionEvent event = new ConsumptionEvent();
        event.setHousehold(householdService.findById(dto.getHouseholdId()));
        event.setIngredient(ingredientService.findById(dto.getIngredientId()));
        if (dto.getLotId() != null) {
            event.setLot(lotRepository.findById(dto.getLotId()).orElse(null));
        }
        event.setQuantity(dto.getQuantity());
        event.setUnit(dto.getUnit());
        event.setSource(dto.getSource());
        if (dto.getMealPlanId() != null) {
            // mealPlan lookup would go here; for now just set null for UNPLANNED
        }
        return consumptionEventRepository.save(event);
    }

    @PostMapping("/events/waste")
    @ResponseStatus(HttpStatus.CREATED)
    public WasteEvent recordWaste(@Valid @RequestBody CreateWasteEventDto dto) {
        WasteEvent event = new WasteEvent();
        event.setHousehold(householdService.findById(dto.getHouseholdId()));
        event.setIngredient(ingredientService.findById(dto.getIngredientId()));
        if (dto.getLotId() != null) {
            event.setLot(lotRepository.findById(dto.getLotId()).orElse(null));
        }
        event.setQuantity(dto.getQuantity());
        event.setUnit(dto.getUnit());
        event.setWasteReason(dto.getWasteReason());

        WasteEvent saved = wasteEventRepository.save(event);

        usageRecordService.recompute(dto.getHouseholdId(), dto.getIngredientId());

        return saved;
    }

    @PostMapping("/events/meal-outcome")
    @ResponseStatus(HttpStatus.CREATED)
    public MealOutcomeEvent recordMealOutcome(@Valid @RequestBody CreateMealOutcomeDto dto) {
        MealOutcomeEvent event = new MealOutcomeEvent();
        event.setMealPlan(mealPlanRepository.findById(dto.getMealPlanId())
                .orElseThrow(() -> new IllegalArgumentException("MealPlan not found: " + dto.getMealPlanId())));
        event.setHousehold(householdService.findById(dto.getHouseholdId()));
        event.setOutcome(dto.getOutcome());
        event.setNotes(dto.getNotes());
        return mealOutcomeEventRepository.save(event);
    }

    @GetMapping("/usage/{ingredientId}")
    public IngredientUsageRecord usage(@PathVariable UUID ingredientId,
                                        @RequestParam UUID householdId) {
        return usageRecordRepository.findByHouseholdIdAndIngredientId(householdId, ingredientId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No usage record for household " + householdId + ", ingredient " + ingredientId));
    }
}
