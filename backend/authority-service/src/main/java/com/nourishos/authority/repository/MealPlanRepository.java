package com.nourishos.authority.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.MealPlan;

public interface MealPlanRepository extends JpaRepository<MealPlan, UUID> {

    Optional<MealPlan> findByMealRequestId(UUID mealRequestId);
}
