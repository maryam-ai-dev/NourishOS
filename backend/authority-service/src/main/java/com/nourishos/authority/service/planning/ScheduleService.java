package com.nourishos.authority.service.planning;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nourishos.authority.domain.Household;
import com.nourishos.authority.domain.MealType;
import com.nourishos.authority.domain.ScheduledMealSlot;
import com.nourishos.authority.domain.WeeklyMealSchedule;
import com.nourishos.authority.dto.CreateScheduleDto;
import com.nourishos.authority.dto.ScheduleResponse;
import com.nourishos.authority.repository.MealPlanRepository;
import com.nourishos.authority.repository.ScheduledMealSlotRepository;
import com.nourishos.authority.repository.WeeklyMealScheduleRepository;
import com.nourishos.authority.service.HouseholdService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final WeeklyMealScheduleRepository scheduleRepository;
    private final ScheduledMealSlotRepository slotRepository;
    private final MealPlanRepository mealPlanRepository;
    private final HouseholdService householdService;

    @Transactional
    public ScheduleResponse create(CreateScheduleDto dto) {
        Household household = householdService.findById(dto.getHouseholdId());

        WeeklyMealSchedule schedule = new WeeklyMealSchedule();
        schedule.setHousehold(household);
        schedule.setWeekStartDate(dto.getWeekStartDate());
        WeeklyMealSchedule saved = scheduleRepository.save(schedule);

        List<ScheduledMealSlot> slots = new ArrayList<>();

        if (dto.getSlots() != null && !dto.getSlots().isEmpty()) {
            for (CreateScheduleDto.SlotDto s : dto.getSlots()) {
                ScheduledMealSlot slot = new ScheduledMealSlot();
                slot.setSchedule(saved);
                slot.setDayOfWeek(s.getDayOfWeek());
                slot.setMealType(MealType.valueOf(s.getMealType()));
                if (s.getMealPlanId() != null) {
                    slot.setMealPlan(mealPlanRepository.findById(s.getMealPlanId()).orElse(null));
                }
                slots.add(slotRepository.save(slot));
            }
        } else {
            // Generate 21 empty slots (7 days x 3 meals)
            for (int day = 1; day <= 7; day++) {
                for (MealType type : MealType.values()) {
                    if (type == MealType.SNACK) continue;
                    ScheduledMealSlot slot = new ScheduledMealSlot();
                    slot.setSchedule(saved);
                    slot.setDayOfWeek(day);
                    slot.setMealType(type);
                    slots.add(slotRepository.save(slot));
                }
            }
        }

        return ScheduleResponse.from(saved, slots);
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getCurrentSchedule(UUID householdId) {
        householdService.findById(householdId);
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        WeeklyMealSchedule schedule = scheduleRepository
                .findByHouseholdIdAndWeekStartDate(householdId, weekStart)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No schedule found for household " + householdId + " week starting " + weekStart));
        List<ScheduledMealSlot> slots = slotRepository.findByScheduleId(schedule.getId());
        return ScheduleResponse.from(schedule, slots);
    }

    @Transactional
    public ScheduleResponse.SlotResponse updateSlot(UUID scheduleId, UUID slotId, String status) {
        ScheduledMealSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found: " + slotId));
        slot.setStatus(status);
        return ScheduleResponse.SlotResponse.from(slotRepository.save(slot));
    }
}
