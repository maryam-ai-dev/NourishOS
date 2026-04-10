package com.nourishos.authority.domain;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "meal_outcome_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MealOutcomeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "meal_plan_id", nullable = false, unique = true)
    private MealPlan mealPlan;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealOutcome outcome;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Column(length = 500)
    private String notes;

    @jakarta.persistence.PrePersist
    void prePersist() {
        if (completedAt == null) {
            completedAt = Instant.now();
        }
    }
}
