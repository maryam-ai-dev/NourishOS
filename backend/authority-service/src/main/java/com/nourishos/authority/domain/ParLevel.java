package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "par_levels", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"household_id", "ingredient_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "preferred_quantity", nullable = false)
    private BigDecimal preferredQuantity;

    @Column(name = "minimum_quantity", nullable = false)
    private BigDecimal minimumQuantity;

    @Column(nullable = false, length = 10)
    private String unit;
}
