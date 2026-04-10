package com.nourishos.authority.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterventionRequestTest {

    @Test
    void resolveSetStatusAndTimestamp() {
        InterventionRequest ir = new InterventionRequest();
        ir.setStatus("PENDING");
        ir.resolve();
        assertEquals("RESOLVED", ir.getStatus());
        assertNotNull(ir.getResolvedAt());
    }

    @Test
    void resolveAlreadyResolvedThrows() {
        InterventionRequest ir = new InterventionRequest();
        ir.setStatus("RESOLVED");
        assertThrows(IllegalStateException.class, ir::resolve);
    }
}
