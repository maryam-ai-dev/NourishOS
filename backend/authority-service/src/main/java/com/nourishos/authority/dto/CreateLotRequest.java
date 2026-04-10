package com.nourishos.authority.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateLotRequest {

    @NotNull(message = "ingredientId is required")
    private UUID ingredientId;

    private UUID storageLocationId;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be positive")
    private BigDecimal quantity;

    @NotBlank(message = "unit is required")
    private String unit;

    private Instant expiryDate;
}
