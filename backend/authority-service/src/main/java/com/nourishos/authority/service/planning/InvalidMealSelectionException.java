package com.nourishos.authority.service.planning;

import java.util.UUID;

public class InvalidMealSelectionException extends RuntimeException {

    public InvalidMealSelectionException(UUID mealOptionId, UUID planId) {
        super("MealOption " + mealOptionId + " is not a candidate for plan " + planId);
    }
}
