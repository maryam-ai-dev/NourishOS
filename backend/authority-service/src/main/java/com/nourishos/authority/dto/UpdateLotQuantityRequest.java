package com.nourishos.authority.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateLotQuantityRequest {

    @NotNull(message = "newQuantity is required")
    private BigDecimal newQuantity;

    private String reason;
}
