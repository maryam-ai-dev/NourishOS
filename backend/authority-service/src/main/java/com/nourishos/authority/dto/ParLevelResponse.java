package com.nourishos.authority.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.nourishos.authority.domain.ParLevel;
import lombok.Data;

@Data
public class ParLevelResponse {

    private UUID id;
    private UUID householdId;
    private UUID ingredientId;
    private BigDecimal preferredQuantity;
    private BigDecimal minimumQuantity;
    private String unit;

    public static ParLevelResponse from(ParLevel parLevel) {
        ParLevelResponse r = new ParLevelResponse();
        r.setId(parLevel.getId());
        r.setHouseholdId(parLevel.getHousehold().getId());
        r.setIngredientId(parLevel.getIngredient().getId());
        r.setPreferredQuantity(parLevel.getPreferredQuantity());
        r.setMinimumQuantity(parLevel.getMinimumQuantity());
        r.setUnit(parLevel.getUnit());
        return r;
    }
}
