package com.nourishos.authority.service.planning;

import java.math.BigDecimal;

public class InvalidSustainabilityScoreException extends RuntimeException {

    public InvalidSustainabilityScoreException(BigDecimal score) {
        super("sustainabilityScore must be between 0 and 1, got: " + score);
    }
}
