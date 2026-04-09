package com.nourishos.authority.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.Household;
import com.nourishos.authority.domain.Ingredient;
import com.nourishos.authority.domain.ParLevel;
import com.nourishos.authority.dto.CreateParLevelRequest;
import com.nourishos.authority.dto.ParLevelResponse;
import com.nourishos.authority.dto.UpdateParLevelRequest;
import com.nourishos.authority.service.HouseholdService;
import com.nourishos.authority.service.inventory.IngredientService;
import com.nourishos.authority.service.inventory.ParLevelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/households/{householdId}/par-levels")
@RequiredArgsConstructor
public class ParLevelController {

    private final ParLevelService parLevelService;
    private final HouseholdService householdService;
    private final IngredientService ingredientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParLevelResponse create(@PathVariable UUID householdId,
                                   @Valid @RequestBody CreateParLevelRequest request) {
        Household household = householdService.findById(householdId);
        Ingredient ingredient = ingredientService.findById(request.getIngredientId());

        ParLevel parLevel = new ParLevel();
        parLevel.setHousehold(household);
        parLevel.setIngredient(ingredient);
        parLevel.setPreferredQuantity(request.getPreferredQuantity());
        parLevel.setMinimumQuantity(request.getMinimumQuantity());
        parLevel.setUnit(request.getUnit());

        return ParLevelResponse.from(parLevelService.create(parLevel));
    }

    @GetMapping
    public List<ParLevelResponse> list(@PathVariable UUID householdId) {
        householdService.findById(householdId);
        return parLevelService.findByHouseholdId(householdId).stream()
                .map(ParLevelResponse::from)
                .toList();
    }

    @PutMapping("/{ingredientId}")
    public ParLevelResponse update(@PathVariable UUID householdId,
                                   @PathVariable UUID ingredientId,
                                   @Valid @RequestBody UpdateParLevelRequest request) {
        return ParLevelResponse.from(parLevelService.update(
                householdId, ingredientId,
                request.getPreferredQuantity(),
                request.getMinimumQuantity(),
                request.getUnit()));
    }
}
