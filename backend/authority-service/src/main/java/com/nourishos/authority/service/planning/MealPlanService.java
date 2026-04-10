package com.nourishos.authority.service.planning;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.MealPlan;
import com.nourishos.authority.domain.MealRequest;
import com.nourishos.authority.domain.ServingProfile;
import com.nourishos.authority.dto.CreateMealPlanDto;
import com.nourishos.authority.repository.MealPlanRepository;
import com.nourishos.authority.repository.ServingProfileRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final ServingProfileRepository servingProfileRepository;
    private final MealRequestService mealRequestService;
    private final ObjectMapper objectMapper;

    @Transactional
    public MealPlan create(CreateMealPlanDto dto) {
        MealRequest request = mealRequestService.findById(dto.getMealRequestId());

        MealPlan plan = new MealPlan();
        plan.setMealRequest(request);
        plan.setServings(dto.getServings());
        MealPlan saved = mealPlanRepository.save(plan);

        ServingProfile profile = new ServingProfile();
        profile.setMealPlan(saved);
        profile.setHouseholdSize(dto.getHouseholdSize() != null ? dto.getHouseholdSize() : dto.getServings());
        profile.setScalingFactor(BigDecimal.valueOf(dto.getServings()));
        if (dto.getParticipatingMemberIds() != null) {
            try {
                profile.setParticipatingMemberIds(
                        objectMapper.writeValueAsString(dto.getParticipatingMemberIds()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize memberIds", e);
            }
        }
        servingProfileRepository.save(profile);

        return saved;
    }

    public MealPlan findById(UUID id) {
        return mealPlanRepository.findById(id)
                .orElseThrow(() -> new MealPlanNotFoundException(id));
    }
}
