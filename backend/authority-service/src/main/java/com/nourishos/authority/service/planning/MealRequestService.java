package com.nourishos.authority.service.planning;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nourishos.authority.domain.Household;
import com.nourishos.authority.domain.MealConstraint;
import com.nourishos.authority.domain.MealRequest;
import com.nourishos.authority.domain.RequestType;
import com.nourishos.authority.repository.MealConstraintRepository;
import com.nourishos.authority.repository.MealRequestRepository;
import com.nourishos.authority.service.HouseholdService;
import com.nourishos.authority.service.recommendation.RecommendationCacheService;
import com.nourishos.authority.service.recommendation.RecommendationCacheService.RankResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealRequestService {

    private static final int DUPLICATE_GUARD_MINUTES = 5;
    private static final int MAX_CANDIDATES = 3;

    private final MealRequestRepository mealRequestRepository;
    private final MealConstraintRepository mealConstraintRepository;
    private final HouseholdService householdService;
    private final RecommendationCacheService recommendationCacheService;

    @Transactional
    public MealRequest createRequest(UUID householdId, RequestType requestType,
                                      MealConstraint constraint) {
        Household household = householdService.findById(householdId);

        if (constraint.getServings() <= 0) {
            throw new InvalidServingsException(constraint.getServings());
        }

        // Duplicate guard
        Instant cutoff = Instant.now().minus(DUPLICATE_GUARD_MINUTES, ChronoUnit.MINUTES);
        mealRequestRepository.findFirstByHouseholdIdAndRequestTypeAndRequestedAtAfter(
                householdId, requestType, cutoff)
                .ifPresent(existing -> {
                    throw new DuplicateMealRequestException(existing.getId());
                });

        MealRequest request = new MealRequest();
        request.setHousehold(household);
        request.setRequestType(requestType);

        MealRequest saved = mealRequestRepository.save(request);

        constraint.setMealRequest(saved);
        mealConstraintRepository.save(constraint);

        // Wire: call FastAPI /recommendation/rank via cache service
        triggerRanking(householdId, constraint);

        return saved;
    }

    private void triggerRanking(UUID householdId, MealConstraint constraint) {
        try {
            Double proteinTarget = constraint.getProteinTargetGrams() != null
                    ? constraint.getProteinTargetGrams().doubleValue() : null;
            RankResponse ranked = recommendationCacheService.getRankedMeals(
                    householdId, proteinTarget, MAX_CANDIDATES);
            log.info("Ranking completed for household {}: {} candidates", householdId, ranked.ranked().size());
        } catch (Exception e) {
            log.warn("Ranking call failed for household {}: {}", householdId, e.getMessage());
        }
    }

    public MealRequest findById(UUID id) {
        return mealRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MealRequest not found: " + id));
    }
}
