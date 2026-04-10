package com.nourishos.authority.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nourishos.authority.domain.WeeklySavingsSnapshot;

public interface WeeklySavingsSnapshotRepository extends JpaRepository<WeeklySavingsSnapshot, UUID> {

    Optional<WeeklySavingsSnapshot> findByHouseholdIdAndWeekStartDate(UUID householdId, LocalDate weekStartDate);
}
