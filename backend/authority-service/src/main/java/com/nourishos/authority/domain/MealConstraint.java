package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "meal_constraints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MealConstraint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "meal_request_id", nullable = false, unique = true)
    private MealRequest mealRequest;

    @Column(nullable = false)
    private int servings;

    @Column(name = "protein_target_grams")
    private BigDecimal proteinTargetGrams;

    @Column(name = "preferred_time")
    private String preferredTime;

    @Column(name = "max_effort")
    private String maxEffort;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "participating_member_ids", nullable = false, columnDefinition = "jsonb")
    private String participatingMemberIds = "[]";
}
