package com.nourishos.authority.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.FoodFlowSnapshot;

public interface FoodFlowSnapshotRepository extends JpaRepository<FoodFlowSnapshot, UUID> {

    Optional<FoodFlowSnapshot> findFirstByHouseholdIdOrderBySnapshotDateDesc(UUID householdId);
}
