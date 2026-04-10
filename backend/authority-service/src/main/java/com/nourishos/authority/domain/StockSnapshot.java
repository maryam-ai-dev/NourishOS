package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class StockSnapshot {

    private UUID householdId;
    private Instant generatedAt;
    private List<LocationGroup> locations;

    @Data
    @AllArgsConstructor
    public static class LocationGroup {
        private UUID locationId;
        private String locationType;
        private String label;
        private List<LotSummary> lots;
    }

    @Data
    @AllArgsConstructor
    public static class LotSummary {
        private UUID lotId;
        private UUID ingredientId;
        private String ingredientName;
        private BigDecimal quantity;
        private String unit;
        private boolean isOpen;
        private Instant expiryDate;
    }
}
