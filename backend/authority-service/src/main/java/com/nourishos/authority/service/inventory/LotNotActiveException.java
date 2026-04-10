package com.nourishos.authority.service.inventory;

import java.util.UUID;

import com.nourishos.authority.domain.LotStatus;

public class LotNotActiveException extends RuntimeException {

    public LotNotActiveException(UUID lotId, LotStatus status) {
        super("Lot " + lotId + " is not ACTIVE (current status: " + status + ")");
    }
}
