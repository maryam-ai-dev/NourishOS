package com.nourishos.authority.service;

public class InvalidNutritionGoalUnitException extends RuntimeException {

    public InvalidNutritionGoalUnitException(String unit) {
        super("Invalid nutrition goal unit: " + unit + ". Must be one of: grams, kcal, percent");
    }
}
