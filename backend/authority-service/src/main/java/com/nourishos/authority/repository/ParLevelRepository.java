package com.nourishos.authority.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.ParLevel;

public interface ParLevelRepository extends JpaRepository<ParLevel, UUID> {

    List<ParLevel> findByHouseholdId(UUID householdId);

    Optional<ParLevel> findByHouseholdIdAndIngredientId(UUID householdId, UUID ingredientId);
}
