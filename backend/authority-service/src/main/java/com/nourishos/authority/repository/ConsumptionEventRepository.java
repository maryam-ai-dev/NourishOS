package com.nourishos.authority.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.ConsumptionEvent;

public interface ConsumptionEventRepository extends JpaRepository<ConsumptionEvent, UUID> {

    List<ConsumptionEvent> findByHouseholdId(UUID householdId);

    List<ConsumptionEvent> findByIngredientId(UUID ingredientId);
}
