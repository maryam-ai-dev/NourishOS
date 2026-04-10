package com.nourishos.authority.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.WeeklyMealSchedule;

public interface WeeklyMealScheduleRepository extends JpaRepository<WeeklyMealSchedule, UUID> {

    Optional<WeeklyMealSchedule> findByHouseholdIdAndWeekStartDate(UUID householdId, LocalDate weekStartDate);
}
