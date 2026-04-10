package com.nourishos.authority.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.nourishos.authority.domain.ConsumptionSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateConsumptionEventDto {

    @NotNull private UUID householdId;
    @NotNull private UUID ingredientId;
    private UUID lotId;
    @NotNull @Positive private BigDecimal quantity;
    @NotBlank private String unit;
    @NotNull private ConsumptionSource source;
    private UUID mealPlanId;
}
