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
@Table(name = "ingredient_lots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientLot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_location_id")
    private StorageLocation storageLocation;

    @Column(nullable = false)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(nullable = false, length = 10)
    private String unit;

    @Column(name = "purchased_at", nullable = false)
    private Instant purchasedAt;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    @Column(name = "is_open", nullable = false)
    private boolean isOpen = false;

    @Column(name = "is_managed", nullable = false)
    private boolean isManaged = true;

    @Column(name = "correction_flag", nullable = false)
    private boolean correctionFlag = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LotStatus status = LotStatus.ACTIVE;

    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    void validateExpiryDate() {
        if (expiryDate != null && purchasedAt != null
                && expiryDate.isBefore(purchasedAt) && !correctionFlag) {
            throw new IllegalStateException("expiryDate cannot be before purchasedAt unless correctionFlag is true");
        }
    }
}
