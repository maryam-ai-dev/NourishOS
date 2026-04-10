package com.nourishos.authority.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.MealConstraint;
import com.nourishos.authority.domain.MealRequest;
import com.nourishos.authority.dto.CreateMealRequestDto;
import com.nourishos.authority.dto.MealRequestResponse;
import com.nourishos.authority.service.planning.MealRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/planning/meal-requests")
@RequiredArgsConstructor
public class MealRequestController {

    private final MealRequestService mealRequestService;
    private final ObjectMapper objectMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MealRequestResponse create(@Valid @RequestBody CreateMealRequestDto dto) {
        MealConstraint constraint = new MealConstraint();
        constraint.setServings(dto.getServings());
        constraint.setProteinTargetGrams(dto.getProteinTargetGrams());
        constraint.setPreferredTime(dto.getPreferredTime());
        constraint.setMaxEffort(dto.getMaxEffort());
        if (dto.getParticipatingMemberIds() != null) {
            try {
                constraint.setParticipatingMemberIds(
                        objectMapper.writeValueAsString(dto.getParticipatingMemberIds()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize memberIds", e);
            }
        }

        MealRequest saved = mealRequestService.createRequest(
                dto.getHouseholdId(), dto.getRequestType(), constraint);
        return MealRequestResponse.from(saved);
    }

    @GetMapping("/{id}")
    public MealRequestResponse get(@PathVariable UUID id) {
        return MealRequestResponse.from(mealRequestService.findById(id));
    }
}
