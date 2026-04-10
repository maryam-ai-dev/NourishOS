package com.nourishos.authority.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.WasteEvent;

public interface WasteEventRepository extends JpaRepository<WasteEvent, UUID> {

    List<WasteEvent> findByHouseholdId(UUID householdId);

    List<WasteEvent> findByIngredientId(UUID ingredientId);

    List<WasteEvent> findByIngredientIdAndWastedAtAfter(UUID ingredientId, Instant after);
}
