package com.nourishos.authority.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.ServingProfile;

public interface ServingProfileRepository extends JpaRepository<ServingProfile, UUID> {

    Optional<ServingProfile> findByMealPlanId(UUID mealPlanId);
}
