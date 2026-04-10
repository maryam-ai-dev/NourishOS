package com.nourishos.authority.service.planning;

import java.util.UUID;

public class MealOptionNotFoundException extends RuntimeException {

    public MealOptionNotFoundException(UUID id) {
        super("MealOption not found: " + id);
    }
}
