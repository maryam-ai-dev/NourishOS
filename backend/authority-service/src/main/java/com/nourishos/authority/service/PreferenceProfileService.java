package com.nourishos.authority.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.DietaryRule;
import com.nourishos.authority.domain.MemberPreferenceProfile;
import com.nourishos.authority.domain.NutritionGoal;
import com.nourishos.authority.dto.UpdatePreferencesRequest;
import com.nourishos.authority.repository.MemberPreferenceProfileRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreferenceProfileService {

    private final MemberPreferenceProfileRepository profileRepository;
    private final HouseholdMemberService memberService;
    private final ObjectMapper objectMapper;

    @Transactional
    public MemberPreferenceProfile updatePreferences(UUID householdId, UUID memberId,
                                                      UpdatePreferencesRequest request) {
        memberService.findMember(householdId, memberId);
        MemberPreferenceProfile profile = profileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        if (request.getDietaryRules() != null) {
            List<DietaryRule> deduplicated = deduplicateRules(request.getDietaryRules());
            profile.setDietaryRestrictions(toJson(deduplicated));
        }
        if (request.getDislikedIngredients() != null) {
            profile.setDislikedIngredients(toJson(request.getDislikedIngredients()));
        }
        if (request.getProteinGoalGrams() != null) {
            profile.setProteinGoalGrams(request.getProteinGoalGrams());
        }
        if (request.getPreferredMealTypes() != null) {
            profile.setPreferredMealTypes(toJson(request.getPreferredMealTypes()));
        }
        if (request.getNutritionGoals() != null) {
            validateNutritionGoalUnits(request.getNutritionGoals());
            profile.setNutritionGoals(toJson(request.getNutritionGoals()));
        }
        if (request.getFavouriteDishes() != null) {
            profile.setFavouriteDishes(toJson(request.getFavouriteDishes()));
        }
        if (request.getDislikedDishes() != null) {
            profile.setDislikedDishes(toJson(request.getDislikedDishes()));
        }
        if (request.getFavouriteCuisines() != null) {
            profile.setFavouriteCuisines(toJson(request.getFavouriteCuisines()));
        }

        return profileRepository.save(profile);
    }

    private List<DietaryRule> deduplicateRules(List<DietaryRule> rules) {
        LinkedHashMap<String, DietaryRule> map = new LinkedHashMap<>();
        for (DietaryRule rule : rules) {
            map.put(rule.getRuleType().name(), rule);
        }
        return new ArrayList<>(map.values());
    }

    private void validateNutritionGoalUnits(List<NutritionGoal> goals) {
        for (NutritionGoal goal : goals) {
            if (!goal.hasValidUnit()) {
                throw new InvalidNutritionGoalUnitException(goal.getUnit());
            }
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }
}
