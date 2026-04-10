package com.nourishos.authority.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.nourishos.authority.domain.Household;
import com.nourishos.authority.domain.HouseholdSettings;
import com.nourishos.authority.domain.PolicySet;
import com.nourishos.authority.dto.CreateHouseholdRequest;
import com.nourishos.authority.dto.UpdateHouseholdRequest;
import com.nourishos.authority.repository.HouseholdRepository;
import com.nourishos.authority.repository.PolicySetRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final PolicySetRepository policySetRepository;

    @Transactional
    public Household create(CreateHouseholdRequest request) {
        Household household = new Household();
        household.setName(request.getName());
        household.setWeeklyBudgetLimit(request.getWeeklyBudgetLimit());

        HouseholdSettings settings = new HouseholdSettings();
        settings.setHousehold(household);
        settings.setWeeklyBudgetLimit(request.getWeeklyBudgetLimit());
        household.setSettings(settings);

        Household saved = householdRepository.save(household);

        PolicySet policySet = new PolicySet();
        policySet.setHousehold(saved);
        policySetRepository.save(policySet);

        return saved;
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
