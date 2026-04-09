package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;
import java.util.UUID;

public class NegativeQuantityException extends RuntimeException {

    public NegativeQuantityException(UUID lotId, BigDecimal currentQty, BigDecimal delta) {
        super("Adjustment would make lot " + lotId + " negative: current=" + currentQty + ", delta=" + delta);
    }
}
