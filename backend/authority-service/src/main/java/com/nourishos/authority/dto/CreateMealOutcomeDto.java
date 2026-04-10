package com.nourishos.authority.dto;

import java.util.UUID;

import com.nourishos.authority.domain.MealOutcome;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMealOutcomeDto {

    @NotNull private UUID mealPlanId;
    @NotNull private UUID householdId;
    @NotNull private MealOutcome outcome;
    private String notes;
}
