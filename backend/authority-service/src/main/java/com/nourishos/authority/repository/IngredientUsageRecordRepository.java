package com.nourishos.authority.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.IngredientUsageRecord;

public interface IngredientUsageRecordRepository extends JpaRepository<IngredientUsageRecord, UUID> {

    Optional<IngredientUsageRecord> findByHouseholdIdAndIngredientId(UUID householdId, UUID ingredientId);

    List<IngredientUsageRecord> findByHouseholdId(UUID householdId);
}
