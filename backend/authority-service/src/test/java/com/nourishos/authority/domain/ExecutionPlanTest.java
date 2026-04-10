package com.nourishos.authority.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionPlanTest {

    @Test
    void transitionFromPendingToInProgressAllowed() {
        ExecutionPlan plan = new ExecutionPlan();
        plan.setStatus(ExecutionStatus.PENDING);
        assertDoesNotThrow(() -> plan.transitionTo(ExecutionStatus.IN_PROGRESS));
        assertEquals(ExecutionStatus.IN_PROGRESS, plan.getStatus());
    }

    @Test
    void transitionFromCompletedThrows() {
        ExecutionPlan plan = new ExecutionPlan();
        plan.setStatus(ExecutionStatus.COMPLETED);
        assertThrows(IllegalStateException.class, () -> plan.transitionTo(ExecutionStatus.IN_PROGRESS));
    }

    @Test
    void transitionFromFailedThrows() {
        ExecutionPlan plan = new ExecutionPlan();
        plan.setStatus(ExecutionStatus.FAILED);
        assertThrows(IllegalStateException.class, () -> plan.transitionTo(ExecutionStatus.PENDING));
    }

    @Test
    void transitionFromAbortedThrows() {
        ExecutionPlan plan = new ExecutionPlan();
        plan.setStatus(ExecutionStatus.ABORTED);
        assertThrows(IllegalStateException.class, () -> plan.transitionTo(ExecutionStatus.IN_PROGRESS));
    }

    @Test
    void transitionFromPausedAllowed() {
        ExecutionPlan plan = new ExecutionPlan();
        plan.setStatus(ExecutionStatus.PAUSED);
        assertDoesNotThrow(() -> plan.transitionTo(ExecutionStatus.IN_PROGRESS));
    }
}
