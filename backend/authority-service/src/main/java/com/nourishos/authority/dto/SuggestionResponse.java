package com.nourishos.authority.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.nourishos.authority.domain.ReplenishmentSuggestion;
import lombok.Data;

@Data
public class SuggestionResponse {

    private UUID id;
    private UUID householdId;
    private UUID ingredientId;
    private BigDecimal suggestedQuantity;
    private String unit;
    private String reason;
    private String urgency;
    private String status;
    private boolean adjustedForWaste;
    private Instant createdAt;

    public static SuggestionResponse from(ReplenishmentSuggestion s) {
        SuggestionResponse r = new SuggestionResponse();
        r.setId(s.getId());
        r.setHouseholdId(s.getHousehold().getId());
        r.setIngredientId(s.getIngredient().getId());
        r.setSuggestedQuantity(s.getSuggestedQuantity());
        r.setUnit(s.getUnit());
        r.setReason(s.getReason());
        r.setUrgency(s.getUrgency());
        r.setStatus(s.getStatus());
        r.setAdjustedForWaste(s.isAdjustedForWaste());
        r.setCreatedAt(s.getCreatedAt());
        return r;
    }
}
