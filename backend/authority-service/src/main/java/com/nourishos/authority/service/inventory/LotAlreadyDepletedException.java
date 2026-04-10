package com.nourishos.authority.service.inventory;

import java.util.UUID;

public class LotAlreadyDepletedException extends RuntimeException {

    public LotAlreadyDepletedException(UUID lotId) {
        super("Lot is already DEPLETED: " + lotId);
    }
}
