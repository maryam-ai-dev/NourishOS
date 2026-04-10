package com.nourishos.authority.service.inventory;

import java.util.UUID;

public class LotAlreadyOpenException extends RuntimeException {

    public LotAlreadyOpenException(UUID lotId) {
        super("Lot is already open: " + lotId);
    }
}
