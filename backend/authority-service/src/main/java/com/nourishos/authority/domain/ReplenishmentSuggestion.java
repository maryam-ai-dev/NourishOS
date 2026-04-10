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
@Table(name = "replenishment_suggestions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReplenishmentSuggestion {

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

    @Column(name = "suggested_quantity", nullable = false)
    private BigDecimal suggestedQuantity;

    @Column(nullable = false, length = 10)
    private String unit;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private String urgency = "MEDIUM";

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "adjusted_for_waste", nullable = false)
    private boolean adjustedForWaste = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replenishment_request_id")
    private ReplenishmentRequest replenishmentRequest;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @jakarta.persistence.PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
