package com.nourishos.authority.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nourishos.authority.domain.ExecutionPlan;
import com.nourishos.authority.domain.ExecutionStep;
import com.nourishos.authority.domain.InterventionRequest;
import lombok.Data;

@Data
public class ExecutionResponse {

    private UUID id;
    private UUID mealPlanId;
    private String status;
    private Instant startedAt;
    private Instant completedAt;
    private Integer estimatedDurationSeconds;
    private List<StepDto> steps;
    private List<InterventionDto> interventions;
    private SessionState sessionState;
    private ActiveIntervention activeIntervention;

    @Data
    public static class StepDto {
        private UUID id;
        private int stepOrder;
        private String actionType;
        private String assignedTo;
        private String status;
        private Integer estimatedDurationSeconds;
        private String ingredientRef;

        public static StepDto from(ExecutionStep s) {
            StepDto d = new StepDto();
            d.setId(s.getId());
            d.setStepOrder(s.getStepOrder());
            d.setActionType(s.getActionType());
            d.setAssignedTo(s.getAssignedTo());
            d.setStatus(s.getStatus());
            d.setEstimatedDurationSeconds(s.getEstimatedDurationSeconds());
            d.setIngredientRef(s.getIngredientRef());
            return d;
        }
    }

    @Data
    public static class InterventionDto {
        private UUID id;
        private UUID stepId;
        private String interventionType;
        private String message;
        private String status;
        private Instant resolvedAt;

        public static InterventionDto from(InterventionRequest ir) {
            InterventionDto d = new InterventionDto();
            d.setId(ir.getId());
            d.setStepId(ir.getStep().getId());
            d.setInterventionType(ir.getInterventionType().name());
            d.setMessage(ir.getMessage());
            d.setStatus(ir.getStatus());
            d.setResolvedAt(ir.getResolvedAt());
            return d;
        }
    }

    @Data
    public static class SessionState {
        private int currentStepIndex;
        private String status;
    }

    @Data
    public static class ActiveIntervention {
        private String interventionId;
        private String type;
    }

    public static ExecutionResponse from(ExecutionPlan plan, List<ExecutionStep> steps,
                                          List<InterventionRequest> interventions) {
        ExecutionResponse r = new ExecutionResponse();
        r.setId(plan.getId());
        r.setMealPlanId(plan.getMealPlan().getId());
        r.setStatus(plan.getStatus().name());
        r.setStartedAt(plan.getStartedAt());
        r.setCompletedAt(plan.getCompletedAt());
        r.setEstimatedDurationSeconds(plan.getEstimatedDurationSeconds());
        r.setSteps(steps.stream().map(StepDto::from).toList());
        r.setInterventions(interventions.stream().map(InterventionDto::from).toList());
        return r;
    }
}
