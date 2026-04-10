package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ingredient_usage_records", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"household_id", "ingredient_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientUsageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "total_consumed_last_4_weeks", nullable = false)
    private BigDecimal totalConsumedLast4Weeks = BigDecimal.ZERO;

    @Column(name = "total_wasted_last_4_weeks", nullable = false)
    private BigDecimal totalWastedLast4Weeks = BigDecimal.ZERO;

    @Column(name = "avg_weekly_usage", nullable = false)
    private BigDecimal avgWeeklyUsage = BigDecimal.ZERO;

    @Column(name = "last_recomputed_at", nullable = false)
    private Instant lastRecomputedAt;

    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    void updateTimestamp() {
        lastRecomputedAt = Instant.now();
    }
}
