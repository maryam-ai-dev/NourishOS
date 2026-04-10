package com.nourishos.authority.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateScheduleDto {

    @NotNull(message = "householdId is required")
    private UUID householdId;

    @NotNull(message = "weekStartDate is required")
    private LocalDate weekStartDate;

    private List<SlotDto> slots;

    @Data
    public static class SlotDto {
        private int dayOfWeek;
        private String mealType;
        private UUID mealPlanId;
    }
}
