package com.nourishos.authority.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nourishos.authority.domain.Household;
import com.nourishos.authority.dto.CreateHouseholdRequest;
import com.nourishos.authority.dto.UpdateHouseholdRequest;
import com.nourishos.authority.repository.HouseholdRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HouseholdService {

    private final HouseholdRepository householdRepository;

    public Household create(CreateHouseholdRequest request) {
        Household household = new Household();
        household.setName(request.getName());
        household.setWeeklyBudgetLimit(request.getWeeklyBudgetLimit());
        return householdRepository.save(household);
    }

    public Household findById(UUID id) {
        return householdRepository.findById(id)
                .orElseThrow(() -> new HouseholdNotFoundException(id));
    }

    public Household update(UUID id, UpdateHouseholdRequest request) {
        Household household = findById(id);
        household.setName(request.getName());
        household.setWeeklyBudgetLimit(request.getWeeklyBudgetLimit());
        return householdRepository.save(household);
    }
}
