package com.nourishos.authority.domain;

import java.math.BigDecimal;
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
@Table(name = "household_budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "household_id", nullable = false, unique = true)
    private UUID householdId;

    @Column(name = "weekly_limit_gbp")
    private BigDecimal weeklyLimitGbp;

    @Column(name = "groceries_limit_gbp")
    private BigDecimal groceriesLimitGbp;

    @Column(name = "pantry_limit_gbp")
    private BigDecimal pantryLimitGbp;

    @Column(name = "other_limit_gbp")
    private BigDecimal otherLimitGbp;
}
