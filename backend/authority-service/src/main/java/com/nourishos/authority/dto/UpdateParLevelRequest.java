package com.nourishos.authority.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateParLevelRequest {

    @NotNull(message = "preferredQuantity is required")
    @Positive(message = "preferredQuantity must be positive")
    private BigDecimal preferredQuantity;

    @NotNull(message = "minimumQuantity is required")
    @Positive(message = "minimumQuantity must be positive")
    private BigDecimal minimumQuantity;

    @NotBlank(message = "unit is required")
    private String unit;
}
