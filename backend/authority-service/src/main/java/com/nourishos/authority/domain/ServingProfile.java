package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "serving_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServingProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "meal_plan_id", nullable = false, unique = true)
    private MealPlan mealPlan;

    @Column(name = "household_size", nullable = false)
    private int householdSize;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "participating_member_ids", nullable = false, columnDefinition = "jsonb")
    private String participatingMemberIds = "[]";

    @Column(name = "scaling_factor", nullable = false)
    private BigDecimal scalingFactor = BigDecimal.ONE;
}
