package com.nourishos.authority.dto;

import java.util.List;

import com.nourishos.authority.domain.DietaryRule;
import com.nourishos.authority.domain.NutritionGoal;
import lombok.Data;

@Data
public class UpdatePreferencesRequest {

    private List<DietaryRule> dietaryRules;
    private List<String> dislikedIngredients;
    private Integer proteinGoalGrams;
    private List<String> preferredMealTypes;
    private List<NutritionGoal> nutritionGoals;
    private List<String> favouriteDishes;
    private List<String> dislikedDishes;
    private List<String> favouriteCuisines;
}
