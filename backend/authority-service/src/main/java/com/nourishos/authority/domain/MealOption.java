package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "meal_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MealOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    @Column(name = "estimated_protein_grams")
    private BigDecimal estimatedProteinGrams;

    @Column(name = "estimated_calories")
    private BigDecimal estimatedCalories;

    @Column(name = "prep_time_minutes")
    private Integer prepTimeMinutes;

    @Column(name = "sustainability_score")
    private BigDecimal sustainabilityScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ingredient_refs", nullable = false, columnDefinition = "jsonb")
    private String ingredientRefs = "[]";
}
