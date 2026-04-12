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
import org.springframework.web.server.ResponseStatusException;

import com.nourishos.authority.domain.MealPreferenceFeedback;
import com.nourishos.authority.domain.MealPreferenceFeedback.FeedbackType;
import com.nourishos.authority.repository.MealPreferenceFeedbackRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/planning/feedback")
@RequiredArgsConstructor
public class MealFeedbackController {

    private final MealPreferenceFeedbackRepository repository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MealPreferenceFeedback create(@RequestBody FeedbackRequest request) {
        if (request.feedbackType == FeedbackType.SWAPPED_OUT && request.swappedToMealOptionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "SWAPPED_OUT feedback requires swappedToMealOptionId");
        }

        MealPreferenceFeedback feedback = new MealPreferenceFeedback();
        feedback.setHouseholdId(request.householdId);
        feedback.setMemberId(request.memberId);
        feedback.setMealOptionId(request.mealOptionId);
        feedback.setFeedbackType(request.feedbackType);
        feedback.setSwappedToMealOptionId(request.swappedToMealOptionId);
        return repository.save(feedback);
    }

    @GetMapping("/{householdId}")
    public java.util.List<MealPreferenceFeedback> list(@PathVariable UUID householdId) {
        return repository.findByHouseholdId(householdId);
    }

    public static class FeedbackRequest {
        public UUID householdId;
        public UUID memberId;
        public UUID mealOptionId;
        public FeedbackType feedbackType;
        public UUID swappedToMealOptionId;
    }
}
