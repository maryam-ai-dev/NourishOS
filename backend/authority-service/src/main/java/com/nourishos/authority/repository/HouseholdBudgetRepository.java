package com.nourishos.authority.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nourishos.authority.domain.HouseholdBudget;

public interface HouseholdBudgetRepository extends JpaRepository<HouseholdBudget, UUID> {

    Optional<HouseholdBudget> findByHouseholdId(UUID householdId);
}
