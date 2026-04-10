package com.nourishos.authority.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.MealPlanCandidate;

public interface MealPlanCandidateRepository extends JpaRepository<MealPlanCandidate, UUID> {

    List<MealPlanCandidate> findByMealPlanId(UUID mealPlanId);
}
