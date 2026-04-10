package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "food_flow_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodFlowSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_waste_grams", nullable = false)
    private BigDecimal totalWasteGrams = BigDecimal.ZERO;

    @Column(name = "total_consumed_grams", nullable = false)
    private BigDecimal totalConsumedGrams = BigDecimal.ZERO;

    @Column(name = "waste_ratio", nullable = false)
    private BigDecimal wasteRatio = BigDecimal.ZERO;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "top_wasted_ingredients", nullable = false, columnDefinition = "jsonb")
    private String topWastedIngredients = "[]";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "top_consumed_ingredients", nullable = false, columnDefinition = "jsonb")
    private String topConsumedIngredients = "[]";
}
