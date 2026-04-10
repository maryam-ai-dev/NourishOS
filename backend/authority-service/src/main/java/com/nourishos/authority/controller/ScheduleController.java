package com.nourishos.authority.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.dto.CreateScheduleDto;
import com.nourishos.authority.dto.ScheduleResponse;
import com.nourishos.authority.service.planning.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/planning/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse create(@Valid @RequestBody CreateScheduleDto dto) {
        return scheduleService.create(dto);
    }

    @GetMapping("/{householdId}/current")
    public ScheduleResponse current(@PathVariable UUID householdId) {
        return scheduleService.getCurrentSchedule(householdId);
    }

    @PatchMapping("/{id}/slots/{slotId}")
    public ScheduleResponse.SlotResponse updateSlot(@PathVariable UUID id,
                                                     @PathVariable UUID slotId,
                                                     @RequestBody Map<String, String> body) {
        return scheduleService.updateSlot(id, slotId, body.get("status"));
    }
}
