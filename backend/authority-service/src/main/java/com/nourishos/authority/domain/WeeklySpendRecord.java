package com.nourishos.authority.domain;

import java.math.BigDecimal;
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
@Table(name = "weekly_spend_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySpendRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "household_id", nullable = false)
    private UUID householdId;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "groceries_spent_gbp", nullable = false)
    private BigDecimal groceriesSpentGbp = BigDecimal.ZERO;

    @Column(name = "pantry_spent_gbp", nullable = false)
    private BigDecimal pantrySpentGbp = BigDecimal.ZERO;

    @Column(name = "other_spent_gbp", nullable = false)
    private BigDecimal otherSpentGbp = BigDecimal.ZERO;

    @Column(name = "total_spent_gbp", nullable = false)
    private BigDecimal totalSpentGbp = BigDecimal.ZERO;
}
