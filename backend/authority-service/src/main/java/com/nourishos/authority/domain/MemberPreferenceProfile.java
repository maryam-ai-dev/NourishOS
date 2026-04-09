package com.nourishos.authority.domain;

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
@Table(name = "member_preference_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberPreferenceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private HouseholdMember member;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "disliked_ingredients", nullable = false, columnDefinition = "jsonb")
    private String dislikedIngredients = "[]";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dietary_restrictions", nullable = false, columnDefinition = "jsonb")
    private String dietaryRestrictions = "[]";

    @Column(name = "protein_goal_grams")
    private Integer proteinGoalGrams;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_meal_types", nullable = false, columnDefinition = "jsonb")
    private String preferredMealTypes = "[]";
}
