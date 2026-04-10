package com.nourishos.authority.controller;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.ExecutionPlan;
import com.nourishos.authority.domain.ExecutionStatus;
import com.nourishos.authority.domain.MealPlan;
import com.nourishos.authority.domain.MealRequest;
import com.nourishos.authority.domain.MealRequestStatus;
import com.nourishos.authority.repository.ExecutionPlanRepository;
import com.nourishos.authority.repository.MealRequestRepository;
import com.nourishos.authority.service.execution.ExecutionSessionCache;
import com.nourishos.authority.service.execution.InterventionStateCache;
import com.nourishos.authority.service.planning.MealPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/executions")
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionPlanRepository executionPlanRepository;
    private final MealPlanService mealPlanService;
    private final MealRequestRepository mealRequestRepository;
    private final ExecutionSessionCache sessionCache;
    private final InterventionStateCache interventionCache;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ExecutionPlan create(@RequestBody java.util.Map<String, UUID> body) {
        UUID mealPlanId = body.get("mealPlanId");
        MealPlan mealPlan = mealPlanService.findById(mealPlanId);

        if (mealPlan.getSelectedMealOption() == null) {
            throw new IllegalStateException("Cannot create execution for MealPlan without selectedMealOptionId");
        }

        ExecutionPlan plan = new ExecutionPlan();
        plan.setMealPlan(mealPlan);
        ExecutionPlan saved = executionPlanRepository.save(plan);

        MealRequest request = mealPlan.getMealRequest();
        request.setStatus(MealRequestStatus.EXECUTING);
        mealRequestRepository.save(request);

        return saved;
    }

    @PostMapping("/{id}/start")
    public ExecutionPlan start(@PathVariable UUID id) {
        ExecutionPlan plan = executionPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ExecutionPlan not found: " + id));
        plan.transitionTo(ExecutionStatus.IN_PROGRESS);
        plan.setStartedAt(Instant.now());
        executionPlanRepository.save(plan);

        sessionCache.writeSession(id, 0, "IN_PROGRESS", plan.getStartedAt().toString());

        return plan;
    }

    @PostMapping("/{id}/pause")
    public ExecutionPlan pause(@PathVariable UUID id) {
        ExecutionPlan plan = executionPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ExecutionPlan not found: " + id));
        plan.transitionTo(ExecutionStatus.PAUSED);
        return executionPlanRepository.save(plan);
    }

    @PostMapping("/{id}/abort")
    public ExecutionPlan abort(@PathVariable UUID id) {
        ExecutionPlan plan = executionPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ExecutionPlan not found: " + id));
        plan.transitionTo(ExecutionStatus.ABORTED);
        executionPlanRepository.save(plan);

        sessionCache.deleteSession(id);

        return plan;
    }
}
