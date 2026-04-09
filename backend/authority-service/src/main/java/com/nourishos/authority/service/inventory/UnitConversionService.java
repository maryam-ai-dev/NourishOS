package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class UnitConversionService {

    private enum Category { MASS, VOLUME, COUNTABLE }

    private static final Map<String, Category> UNIT_CATEGORIES = Map.of(
            "g", Category.MASS,
            "kg", Category.MASS,
            "ml", Category.VOLUME,
            "l", Category.VOLUME,
            "unit", Category.COUNTABLE
    );

    // Conversion factors to canonical unit within category (g for mass, ml for volume)
    private static final Map<String, BigDecimal> TO_CANONICAL = Map.of(
            "g", BigDecimal.ONE,
            "kg", new BigDecimal("1000"),
            "ml", BigDecimal.ONE,
            "l", new BigDecimal("1000"),
            "unit", BigDecimal.ONE
    );

    public BigDecimal convert(BigDecimal quantity, String fromUnit, String toUnit) {
        if (fromUnit.equals(toUnit)) {
            return quantity;
        }

        Category fromCategory = getCategory(fromUnit);
        Category toCategory = getCategory(toUnit);

        if (fromCategory != toCategory) {
            throw new IncompatibleUnitsException(fromUnit, toUnit);
        }

        BigDecimal canonical = quantity.multiply(TO_CANONICAL.get(fromUnit));
        return canonical.divide(TO_CANONICAL.get(toUnit), 6, RoundingMode.HALF_UP)
                .stripTrailingZeros();
    }

    public BigDecimal convert(double quantity, String fromUnit, String toUnit) {
        return convert(BigDecimal.valueOf(quantity), fromUnit, toUnit);
    }

    public boolean areCompatible(String unitA, String unitB) {
        return getCategory(unitA) == getCategory(unitB);
    }

    public Set<String> supportedUnits() {
        return UNIT_CATEGORIES.keySet();
    }

    private Category getCategory(String unit) {
        Category category = UNIT_CATEGORIES.get(unit);
        if (category == null) {
            throw new IllegalArgumentException("Unknown unit: " + unit);
        }
        return category;
    }
}
