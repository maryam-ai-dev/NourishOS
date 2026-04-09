package com.nourishos.authority.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.nourishos.authority.domain.HouseholdSettings;
import lombok.Data;

@Data
public class HouseholdSettingsResponse {

    private UUID id;
    private String effortTolerance;
    private String sustainabilityPriority;
    private BigDecimal weeklyBudgetLimit;
    private int defaultServings;

    public static HouseholdSettingsResponse from(HouseholdSettings settings) {
        HouseholdSettingsResponse r = new HouseholdSettingsResponse();
        r.setId(settings.getId());
        r.setEffortTolerance(settings.getEffortTolerance());
        r.setSustainabilityPriority(settings.getSustainabilityPriority());
        r.setWeeklyBudgetLimit(settings.getWeeklyBudgetLimit());
        r.setDefaultServings(settings.getDefaultServings());
        return r;
    }
}
