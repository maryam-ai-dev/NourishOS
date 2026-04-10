package com.nourishos.authority.service.planning;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.MealOption;
import com.nourishos.authority.domain.MealPlan;
import com.nourishos.authority.domain.MealPlanCandidate;
import com.nourishos.authority.domain.MealRequest;
import com.nourishos.authority.domain.MealRequestStatus;
import com.nourishos.authority.domain.ServingProfile;
import com.nourishos.authority.dto.CreateMealPlanDto;
import com.nourishos.authority.dto.MealPlanResponse;
import com.nourishos.authority.repository.MealOptionRepository;
import com.nourishos.authority.repository.MealPlanCandidateRepository;
import com.nourishos.authority.repository.MealPlanRepository;
import com.nourishos.authority.repository.MealRequestRepository;
import com.nourishos.authority.repository.ServingProfileRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final MealPlanCandidateRepository candidateRepository;
    private final ServingProfileRepository servingProfileRepository;
    private final MealRequestRepository mealRequestRepository;
    private final MealOptionRepository mealOptionRepository;
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

    @Transactional(readOnly = true)
    public MealPlanResponse getFullPlan(UUID id) {
        MealPlan plan = findById(id);
        List<MealPlanCandidate> candidates = candidateRepository.findByMealPlanId(id);
        ServingProfile profile = servingProfileRepository.findByMealPlanId(id).orElse(null);
        return MealPlanResponse.from(plan, candidates, profile);
    }

    @Transactional
    public MealPlanResponse selectMeal(UUID planId, UUID selectedMealOptionId) {
        MealPlan plan = findById(planId);

        List<MealPlanCandidate> candidates = candidateRepository.findByMealPlanId(planId);
        boolean isCandidate = candidates.stream()
                .anyMatch(c -> c.getMealOption().getId().equals(selectedMealOptionId));

        if (!isCandidate) {
            throw new InvalidMealSelectionException(selectedMealOptionId, planId);
        }

        MealOption mealOption = mealOptionRepository.findById(selectedMealOptionId)
                .orElseThrow(() -> new IllegalArgumentException("MealOption not found: " + selectedMealOptionId));
        plan.setSelectedMealOption(mealOption);
        mealPlanRepository.save(plan);

        MealRequest request = plan.getMealRequest();
        request.setStatus(MealRequestStatus.PLANNED);
        mealRequestRepository.save(request);

        ServingProfile profile = servingProfileRepository.findByMealPlanId(planId).orElse(null);
        return MealPlanResponse.from(plan, candidates, profile);
    }
}
