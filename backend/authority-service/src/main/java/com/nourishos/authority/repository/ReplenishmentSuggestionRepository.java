package com.nourishos.authority.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.ReplenishmentSuggestion;

public interface ReplenishmentSuggestionRepository extends JpaRepository<ReplenishmentSuggestion, UUID> {

    List<ReplenishmentSuggestion> findByHouseholdId(UUID householdId);

    List<ReplenishmentSuggestion> findByHouseholdIdAndStatus(UUID householdId, String status);
}
