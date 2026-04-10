package com.nourishos.authority.service.planning;

public class InvalidServingsException extends RuntimeException {

    public InvalidServingsException(int servings) {
        super("servings must be > 0, got: " + servings);
    }
}
