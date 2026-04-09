package com.nourishos.authority.dto;

import com.nourishos.authority.domain.AgeGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMemberRequest {

    @NotBlank(message = "displayName is required")
    private String displayName;

    @NotNull(message = "ageGroup is required")
    private AgeGroup ageGroup;

    private String effortSensitivity = "MEDIUM";
    private boolean participatesInMealPlanning = true;
}
