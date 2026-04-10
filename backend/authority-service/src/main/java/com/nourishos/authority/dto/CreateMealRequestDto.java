package com.nourishos.authority.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.nourishos.authority.domain.RequestType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMealRequestDto {

    @NotNull(message = "householdId is required")
    private UUID householdId;

    @NotNull(message = "requestType is required")
    private RequestType requestType;

    @NotNull(message = "servings is required")
    private Integer servings;

    private BigDecimal proteinTargetGrams;
    private String preferredTime;
    private String maxEffort;
    private List<UUID> participatingMemberIds;
}
