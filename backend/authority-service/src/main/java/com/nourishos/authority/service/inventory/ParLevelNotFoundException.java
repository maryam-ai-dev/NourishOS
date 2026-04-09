package com.nourishos.authority.service.inventory;

import java.util.UUID;

public class ParLevelNotFoundException extends RuntimeException {

    public ParLevelNotFoundException(UUID householdId, UUID ingredientId) {
        super("ParLevel not found for household=" + householdId + ", ingredient=" + ingredientId);
    }
}
