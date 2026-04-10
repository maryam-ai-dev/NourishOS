package com.nourishos.authority.service.planning;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.Ingredient;
import com.nourishos.authority.domain.IngredientRef;
import com.nourishos.authority.domain.MealOption;
import com.nourishos.authority.repository.MealOptionRepository;
import com.nourishos.authority.service.inventory.IngredientService;
import com.nourishos.authority.service.inventory.UnitConversionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MealOptionService {

    private final MealOptionRepository mealOptionRepository;
    private final IngredientService ingredientService;
    private final UnitConversionService unitConversionService;
    private final ObjectMapper objectMapper;

    public MealOption create(MealOption mealOption) {
        validateSustainabilityScore(mealOption.getSustainabilityScore());
        validateIngredientRefs(mealOption.getIngredientRefs());
        return mealOptionRepository.save(mealOption);
    }

    public MealOption findById(UUID id) {
        return mealOptionRepository.findById(id)
                .orElseThrow(() -> new MealOptionNotFoundException(id));
    }

    public List<MealOption> findAll() {
        return mealOptionRepository.findAll();
    }

    public MealOption update(UUID id, MealOption updated) {
        MealOption existing = findById(id);
        existing.setName(updated.getName());
        existing.setMealType(updated.getMealType());
        existing.setEstimatedProteinGrams(updated.getEstimatedProteinGrams());
        existing.setEstimatedCalories(updated.getEstimatedCalories());
        existing.setPrepTimeMinutes(updated.getPrepTimeMinutes());
        validateSustainabilityScore(updated.getSustainabilityScore());
        existing.setSustainabilityScore(updated.getSustainabilityScore());
        validateIngredientRefs(updated.getIngredientRefs());
        existing.setIngredientRefs(updated.getIngredientRefs());
        return mealOptionRepository.save(existing);
    }

    private void validateIngredientRefs(String ingredientRefsJson) {
        if (ingredientRefsJson == null || ingredientRefsJson.equals("[]")) {
            return;
        }
        List<IngredientRef> refs;
        try {
            refs = objectMapper.readValue(ingredientRefsJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid ingredientRefs JSON: " + e.getMessage());
        }
        for (IngredientRef ref : refs) {
            Ingredient ingredient = ingredientService.findById(ref.getIngredientId());
            if (!unitConversionService.areCompatible(ref.getUnit(), ingredient.getDefaultUnit())) {
                throw new IllegalArgumentException(
                        "Unit '" + ref.getUnit() + "' is incompatible with ingredient '"
                                + ingredient.getName() + "' defaultUnit '" + ingredient.getDefaultUnit() + "'");
            }
        }
    }

    private void validateSustainabilityScore(BigDecimal score) {
        if (score != null) {
            if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.ONE) > 0) {
                throw new InvalidSustainabilityScoreException(score);
            }
        }
    }
}
