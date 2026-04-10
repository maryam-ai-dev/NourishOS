package com.nourishos.authority.service.planning;

import java.util.UUID;

public class DuplicateMealRequestException extends RuntimeException {

    private final UUID existingRequestId;

    public DuplicateMealRequestException(UUID existingRequestId) {
        super("Duplicate meal request within 5 minutes. Existing request: " + existingRequestId);
        this.existingRequestId = existingRequestId;
    }

    public UUID getExistingRequestId() {
        return existingRequestId;
    }
}
