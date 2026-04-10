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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MealRequestService {

    private static final int DUPLICATE_GUARD_MINUTES = 5;

    private final MealRequestRepository mealRequestRepository;
    private final MealConstraintRepository mealConstraintRepository;
    private final HouseholdService householdService;

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

        return saved;
    }

    public MealRequest findById(UUID id) {
        return mealRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MealRequest not found: " + id));
    }
}
