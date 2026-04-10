package com.nourishos.authority.domain;

import java.util.Set;

public enum ExecutionStatus {
    PENDING,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    FAILED,
    ABORTED;

    private static final Set<ExecutionStatus> TERMINAL = Set.of(COMPLETED, FAILED, ABORTED);

    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }
}
