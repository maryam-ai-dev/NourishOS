package com.nourishos.authority.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionGoal {

    private String goalType;
    private double targetValue;
    private String unit;

    private static final java.util.Set<String> VALID_UNITS =
            java.util.Set.of("grams", "kcal", "percent");

    public boolean hasValidUnit() {
        return unit != null && VALID_UNITS.contains(unit);
    }
}
