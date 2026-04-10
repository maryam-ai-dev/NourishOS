package com.nourishos.authority.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nourishos.authority.domain.WeeklySpendRecord;

public interface WeeklySpendRecordRepository extends JpaRepository<WeeklySpendRecord, UUID> {

    Optional<WeeklySpendRecord> findByHouseholdIdAndWeekStartDate(UUID householdId, LocalDate weekStartDate);
}
