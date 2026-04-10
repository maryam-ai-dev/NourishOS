package com.nourishos.authority.domain;

import java.math.BigDecimal;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "waste_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WasteEvent {

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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private IngredientLot lot;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false, length = 10)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "waste_reason", nullable = false)
    private WasteReason wasteReason;

    @Column(name = "wasted_at", nullable = false)
    private Instant wastedAt;

    @jakarta.persistence.PrePersist
    void prePersist() {
        if (wastedAt == null) {
            wastedAt = Instant.now();
        }
    }
}
