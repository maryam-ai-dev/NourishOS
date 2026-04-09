package com.nourishos.authority.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.dto.CreateHouseholdRequest;
import com.nourishos.authority.dto.HouseholdResponse;
import com.nourishos.authority.dto.UpdateHouseholdRequest;
import com.nourishos.authority.service.HouseholdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/households")
@RequiredArgsConstructor
public class HouseholdController {

    private final HouseholdService householdService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HouseholdResponse create(@Valid @RequestBody CreateHouseholdRequest request) {
        return HouseholdResponse.from(householdService.create(request));
    }

    @GetMapping("/{id}")
    public HouseholdResponse get(@PathVariable UUID id) {
        return HouseholdResponse.from(householdService.findById(id));
    }

    @PutMapping("/{id}")
    public HouseholdResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateHouseholdRequest request) {
        return HouseholdResponse.from(householdService.update(id, request));
    }
}
