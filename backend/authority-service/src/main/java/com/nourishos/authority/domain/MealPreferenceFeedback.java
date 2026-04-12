package com.nourishos.authority.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "meal_preference_feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MealPreferenceFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "household_id", nullable = false)
    private UUID householdId;

    @Column(name = "member_id")
    private UUID memberId;

    @Column(name = "meal_option_id", nullable = false)
    private UUID mealOptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 30)
    private FeedbackType feedbackType;

    @Column(name = "swapped_to_meal_option_id")
    private UUID swappedToMealOptionId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @jakarta.persistence.PrePersist
    void prePersist() {
        if (recordedAt == null) recordedAt = Instant.now();
    }

    public enum FeedbackType {
        SWAPPED_OUT, CONFIRMED, REJECTED, COMPLETED
    }
}
