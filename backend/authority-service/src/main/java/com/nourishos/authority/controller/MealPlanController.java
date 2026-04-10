package com.nourishos.authority.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.MealPlan;
import com.nourishos.authority.dto.CreateMealPlanDto;
import com.nourishos.authority.service.planning.MealPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/planning/meal-plans")
@RequiredArgsConstructor
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MealPlan create(@Valid @RequestBody CreateMealPlanDto dto) {
        return mealPlanService.create(dto);
    }
}
