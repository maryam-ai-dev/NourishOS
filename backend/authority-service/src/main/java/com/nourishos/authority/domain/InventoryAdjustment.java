package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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
@Table(name = "inventory_adjustments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private IngredientLot lot;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false)
    private AdjustmentType adjustmentType;

    @Column(name = "quantity_delta", nullable = false)
    private BigDecimal quantityDelta;

    @Column(nullable = false, length = 10)
    private String unit;

    @Column(length = 500)
    private String reason;

    @Column(name = "adjusted_at", nullable = false)
    private Instant adjustedAt;

    @jakarta.persistence.PrePersist
    void prePersist() {
        if (adjustedAt == null) {
            adjustedAt = Instant.now();
        }
    }
}
