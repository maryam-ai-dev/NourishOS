package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "savings_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySavingsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "household_id", nullable = false)
    private UUID householdId;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "saved_from_waste_gbp")
    private BigDecimal savedFromWasteGbp;

    @Column(name = "previous_week_saved_gbp")
    private BigDecimal previousWeekSavedGbp;

    @Column(name = "waste_items_this_week", nullable = false)
    private int wasteItemsThisWeek;

    @Column(name = "waste_items_previous_week", nullable = false)
    private int wasteItemsPreviousWeek;

    @Column(name = "meals_completed_rate", nullable = false)
    private BigDecimal mealsCompletedRate = BigDecimal.ZERO;

    @Column(name = "total_spent_gbp", nullable = false)
    private BigDecimal totalSpentGbp = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @jakarta.persistence.PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
