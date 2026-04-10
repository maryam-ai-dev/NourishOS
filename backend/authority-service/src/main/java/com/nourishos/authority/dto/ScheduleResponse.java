package com.nourishos.authority.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.nourishos.authority.domain.ScheduledMealSlot;
import com.nourishos.authority.domain.WeeklyMealSchedule;
import lombok.Data;

@Data
public class ScheduleResponse {

    private UUID id;
    private UUID householdId;
    private LocalDate weekStartDate;
    private List<SlotResponse> slots;

    @Data
    public static class SlotResponse {
        private UUID id;
        private int dayOfWeek;
        private String mealType;
        private UUID mealPlanId;
        private String status;

        public static SlotResponse from(ScheduledMealSlot slot) {
            SlotResponse r = new SlotResponse();
            r.setId(slot.getId());
            r.setDayOfWeek(slot.getDayOfWeek());
            r.setMealType(slot.getMealType().name());
            r.setMealPlanId(slot.getMealPlan() != null ? slot.getMealPlan().getId() : null);
            r.setStatus(slot.getStatus());
            return r;
        }
    }

    public static ScheduleResponse from(WeeklyMealSchedule schedule, List<ScheduledMealSlot> slots) {
        ScheduleResponse r = new ScheduleResponse();
        r.setId(schedule.getId());
        r.setHouseholdId(schedule.getHousehold().getId());
        r.setWeekStartDate(schedule.getWeekStartDate());
        r.setSlots(slots.stream().map(SlotResponse::from).toList());
        return r;
    }
}
