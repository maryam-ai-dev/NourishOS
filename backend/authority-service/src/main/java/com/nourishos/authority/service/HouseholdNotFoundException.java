package com.nourishos.authority.service;

import java.util.UUID;

public class HouseholdNotFoundException extends RuntimeException {

    public HouseholdNotFoundException(UUID id) {
        super("Household not found: " + id);
    }
}
