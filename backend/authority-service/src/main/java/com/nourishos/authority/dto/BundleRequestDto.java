package com.nourishos.authority.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BundleRequestDto {

    @NotNull(message = "householdId is required")
    private UUID householdId;

    @NotEmpty(message = "suggestionIds must not be empty")
    private List<UUID> suggestionIds;
}
