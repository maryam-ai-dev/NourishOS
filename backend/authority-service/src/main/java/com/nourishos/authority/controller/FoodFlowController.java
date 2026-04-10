package com.nourishos.authority.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.ConsumptionEvent;
import com.nourishos.authority.domain.FoodFlowSnapshot;
import com.nourishos.authority.dto.CreateConsumptionEventDto;
import com.nourishos.authority.repository.ConsumptionEventRepository;
import com.nourishos.authority.repository.IngredientLotRepository;
import com.nourishos.authority.service.HouseholdService;
import com.nourishos.authority.service.inventory.FoodFlowSnapshotService;
import com.nourishos.authority.service.inventory.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/foodflow")
@RequiredArgsConstructor
public class FoodFlowController {

    private final FoodFlowSnapshotService snapshotService;
    private final ConsumptionEventRepository consumptionEventRepository;
    private final HouseholdService householdService;
    private final IngredientService ingredientService;
    private final IngredientLotRepository lotRepository;

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
}
