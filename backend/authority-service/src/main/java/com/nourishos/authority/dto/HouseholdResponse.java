package com.nourishos.authority.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.nourishos.authority.domain.Household;
import lombok.Data;

@Data
public class HouseholdResponse {

    private UUID id;
    private String name;
    private BigDecimal weeklyBudgetLimit;
    private Instant createdAt;

    public static HouseholdResponse from(Household household) {
        HouseholdResponse response = new HouseholdResponse();
        response.setId(household.getId());
        response.setName(household.getName());
        response.setWeeklyBudgetLimit(household.getWeeklyBudgetLimit());
        response.setCreatedAt(household.getCreatedAt());
        return response;
    }
}
