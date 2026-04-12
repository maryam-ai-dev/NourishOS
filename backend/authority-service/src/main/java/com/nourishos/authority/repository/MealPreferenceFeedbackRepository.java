package com.nourishos.authority.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nourishos.authority.domain.MealPreferenceFeedback;

public interface MealPreferenceFeedbackRepository extends JpaRepository<MealPreferenceFeedback, UUID> {

    List<MealPreferenceFeedback> findByHouseholdId(UUID householdId);

    List<MealPreferenceFeedback> findByHouseholdIdAndMealOptionId(UUID householdId, UUID mealOptionId);
}
