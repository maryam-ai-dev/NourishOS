package com.nourishos.authority.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.MealConstraint;

public interface MealConstraintRepository extends JpaRepository<MealConstraint, UUID> {

    Optional<MealConstraint> findByMealRequestId(UUID mealRequestId);
}
