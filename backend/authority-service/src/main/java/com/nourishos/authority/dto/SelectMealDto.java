package com.nourishos.authority.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SelectMealDto {

    @NotNull(message = "selectedMealOptionId is required")
    private UUID selectedMealOptionId;
}
