package com.nourishos.authority.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.MealOutcomeEvent;

public interface MealOutcomeEventRepository extends JpaRepository<MealOutcomeEvent, UUID> {

    Optional<MealOutcomeEvent> findByMealPlanId(UUID mealPlanId);

    List<MealOutcomeEvent> findByHouseholdId(UUID householdId);
}
