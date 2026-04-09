package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "household_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "household_id", nullable = false, unique = true)
    private Household household;

    @Column(name = "effort_tolerance", nullable = false)
    private String effortTolerance = "MEDIUM";

    @Column(name = "sustainability_priority", nullable = false)
    private String sustainabilityPriority = "MEDIUM";

    @Column(name = "weekly_budget_limit")
    private BigDecimal weeklyBudgetLimit;

    @Column(name = "default_servings", nullable = false)
    private int defaultServings = 4;
}
