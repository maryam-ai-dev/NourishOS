package com.nourishos.authority.service.planning;

import java.util.UUID;

public class MealPlanNotFoundException extends RuntimeException {

    public MealPlanNotFoundException(UUID id) {
        super("MealPlan not found: " + id);
    }
}
