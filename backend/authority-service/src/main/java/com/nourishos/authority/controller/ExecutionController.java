package com.nourishos.authority.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.ExecutionPlan;
import com.nourishos.authority.domain.ExecutionStep;
import com.nourishos.authority.domain.InterventionRequest;
import com.nourishos.authority.domain.ExecutionStatus;
import com.nourishos.authority.domain.MealPlan;
import com.nourishos.authority.domain.MealRequest;
import com.nourishos.authority.domain.MealRequestStatus;
import com.nourishos.authority.dto.ExecutionResponse;
import com.nourishos.authority.repository.ExecutionPlanRepository;
import com.nourishos.authority.repository.ExecutionStepRepository;
import com.nourishos.authority.repository.InterventionRequestRepository;
import com.nourishos.authority.repository.MealRequestRepository;
import com.nourishos.authority.service.execution.ExecutionSessionCache;
import com.nourishos.authority.service.execution.InterventionStateCache;
import com.nourishos.authority.service.planning.MealPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/executions")
@RequiredArgsConstructor
@Slf4j
public class ExecutionController {

    private final ExecutionPlanRepository executionPlanRepository;
    private final ExecutionStepRepository stepRepository;
    private final InterventionRequestRepository interventionRepository;
    private final MealPlanService mealPlanService;
    private final MealRequestRepository mealRequestRepository;
    private final ExecutionSessionCache sessionCache;
    private final InterventionStateCache interventionCache;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

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

        // Wire: call robotics simulation /run
        try {
            var simPayload = java.util.Map.of("executionId", id.toString());
            restTemplate.postForObject("http://localhost:8001/run", simPayload, String.class);
            log.info("Simulation started for execution {}", id);
        } catch (Exception e) {
            log.warn("Simulation call failed for execution {}: {}", id, e.getMessage());
        }

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

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ExecutionResponse get(@PathVariable UUID id) {
        ExecutionPlan plan = executionPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ExecutionPlan not found: " + id));
        List<ExecutionStep> steps = stepRepository.findByPlanIdOrderByStepOrder(id);
        List<InterventionRequest> interventions = interventionRepository.findByPlanId(id);

        ExecutionResponse response = ExecutionResponse.from(plan, steps, interventions);

        // Redis enrichment
        String sessionJson = sessionCache.getSession(id);
        if (sessionJson != null) {
            try {
                var sessionData = objectMapper.readValue(sessionJson,
                        ExecutionSessionCache.SessionData.class);
                ExecutionResponse.SessionState ss = new ExecutionResponse.SessionState();
                ss.setCurrentStepIndex(sessionData.stepIndex());
                ss.setStatus(sessionData.status());
                response.setSessionState(ss);
            } catch (JsonProcessingException ignored) {}
        }

        String interventionJson = interventionCache.getIntervention(id);
        if (interventionJson != null) {
            try {
                var intData = objectMapper.readValue(interventionJson,
                        InterventionStateCache.InterventionData.class);
                ExecutionResponse.ActiveIntervention ai = new ExecutionResponse.ActiveIntervention();
                ai.setInterventionId(intData.interventionId());
                ai.setType(intData.interventionType());
                response.setActiveIntervention(ai);
            } catch (JsonProcessingException ignored) {}
        }

        return response;
    }

    @PostMapping("/{id}/interventions/{iid}/resolve")
    @Transactional
    public InterventionRequest resolveIntervention(@PathVariable UUID id, @PathVariable UUID iid) {
        InterventionRequest ir = interventionRepository.findById(iid)
                .orElseThrow(() -> new IllegalArgumentException("Intervention not found: " + iid));
        ir.resolve();
        interventionRepository.save(ir);

        interventionCache.deleteIntervention(id);

        return ir;
    }

    @PostMapping("/{id}/steps/{stepId}/complete")
    @Transactional
    public ExecutionStep completeStep(@PathVariable UUID id, @PathVariable UUID stepId) {
        ExecutionStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new IllegalArgumentException("Step not found: " + stepId));
        step.setStatus("COMPLETE");
        step.setCompletedAt(Instant.now());
        stepRepository.save(step);

        // Update Redis session with next step index
        List<ExecutionStep> steps = stepRepository.findByPlanIdOrderByStepOrder(id);
        int nextIndex = -1;
        for (int i = 0; i < steps.size(); i++) {
            if ("PENDING".equals(steps.get(i).getStatus())) {
                nextIndex = i;
                break;
            }
        }

        if (nextIndex >= 0) {
            sessionCache.updateStepIndex(id, nextIndex, "IN_PROGRESS");
        } else {
            // All steps complete
            ExecutionPlan plan = executionPlanRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("ExecutionPlan not found: " + id));
            plan.transitionTo(ExecutionStatus.COMPLETED);
            plan.setCompletedAt(Instant.now());
            executionPlanRepository.save(plan);
            sessionCache.deleteSession(id);
        }

        return step;
    }
}
