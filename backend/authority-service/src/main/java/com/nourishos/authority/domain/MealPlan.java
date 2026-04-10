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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "meal_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_request_id", nullable = false)
    private MealRequest mealRequest;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_meal_option_id")
    private MealOption selectedMealOption;

    @Column(nullable = false)
    private int servings;

    @Column(name = "planned_time")
    private Instant plannedTime;

    @Column(name = "protein_score_snapshot")
    private BigDecimal proteinScoreSnapshot;

    @Column(name = "waste_score_snapshot")
    private BigDecimal wasteScoreSnapshot;

    @Column(name = "reliability_score_snapshot")
    private BigDecimal reliabilityScoreSnapshot;
}
