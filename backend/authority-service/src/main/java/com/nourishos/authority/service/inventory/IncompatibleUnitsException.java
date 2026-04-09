package com.nourishos.authority.service.inventory;

public class IncompatibleUnitsException extends RuntimeException {

    public IncompatibleUnitsException(String fromUnit, String toUnit) {
        super("Cannot convert between incompatible units: " + fromUnit + " → " + toUnit);
    }
}
