package com.nourishos.authority.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateMealPlanDto {

    @NotNull(message = "mealRequestId is required")
    private UUID mealRequestId;

    @NotNull(message = "servings is required")
    @Positive(message = "servings must be > 0")
    private Integer servings;

    private Integer householdSize;
    private List<UUID> participatingMemberIds;
}
