package com.nourishos.authority.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpdateHouseholdRequest {

    @NotBlank(message = "name is required")
    private String name;

    @PositiveOrZero(message = "weeklyBudgetLimit must not be negative")
    private BigDecimal weeklyBudgetLimit;
}
