package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientRef {

    private UUID ingredientId;
    private BigDecimal baseQuantity;
    private String unit;
    private boolean optional;
    private boolean substitutable;
}
