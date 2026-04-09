package com.nourishos.authority.service.inventory;

public class InvalidCanonicalUnitException extends RuntimeException {

    public InvalidCanonicalUnitException(String unit) {
        super("Invalid canonical unit: " + unit + ". Must be one of: g, ml, unit");
    }
}
