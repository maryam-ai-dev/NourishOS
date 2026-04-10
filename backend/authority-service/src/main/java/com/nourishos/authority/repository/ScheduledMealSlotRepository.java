package com.nourishos.authority.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.ScheduledMealSlot;

public interface ScheduledMealSlotRepository extends JpaRepository<ScheduledMealSlot, UUID> {

    List<ScheduledMealSlot> findByScheduleId(UUID scheduleId);
}
