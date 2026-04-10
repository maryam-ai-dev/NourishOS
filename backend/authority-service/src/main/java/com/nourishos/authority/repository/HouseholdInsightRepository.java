package com.nourishos.authority.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.HouseholdInsight;

public interface HouseholdInsightRepository extends JpaRepository<HouseholdInsight, UUID> {

    List<HouseholdInsight> findByHouseholdIdOrderByCreatedAtDesc(UUID householdId);

    Optional<HouseholdInsight> findByHouseholdIdAndSnapshotWeekAndCategory(
            UUID householdId, LocalDate snapshotWeek, String category);
}
