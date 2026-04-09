package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nourishos.authority.domain.Ingredient;
import com.nourishos.authority.domain.ParLevel;
import com.nourishos.authority.repository.ParLevelRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParLevelService {

    private final ParLevelRepository parLevelRepository;
    private final IngredientService ingredientService;
    private final UnitConversionService unitConversionService;

    public ParLevel create(ParLevel parLevel) {
        validate(parLevel);
        return parLevelRepository.save(parLevel);
    }

    public List<ParLevel> findByHouseholdId(UUID householdId) {
        return parLevelRepository.findByHouseholdId(householdId);
    }

    public ParLevel findByHouseholdAndIngredient(UUID householdId, UUID ingredientId) {
        return parLevelRepository.findByHouseholdIdAndIngredientId(householdId, ingredientId)
                .orElseThrow(() -> new ParLevelNotFoundException(householdId, ingredientId));
    }

    public ParLevel update(UUID householdId, UUID ingredientId, BigDecimal preferredQuantity,
                           BigDecimal minimumQuantity, String unit) {
        ParLevel parLevel = findByHouseholdAndIngredient(householdId, ingredientId);
        parLevel.setPreferredQuantity(preferredQuantity);
        parLevel.setMinimumQuantity(minimumQuantity);
        parLevel.setUnit(unit);
        validate(parLevel);
        return parLevelRepository.save(parLevel);
    }

    private void validate(ParLevel parLevel) {
        if (parLevel.getMinimumQuantity().compareTo(parLevel.getPreferredQuantity()) > 0) {
            throw new InvalidParLevelException(
                    "minimumQuantity (" + parLevel.getMinimumQuantity()
                            + ") must be <= preferredQuantity (" + parLevel.getPreferredQuantity() + ")");
        }

        Ingredient ingredient = ingredientService.findById(parLevel.getIngredient().getId());
        if (!unitConversionService.areCompatible(parLevel.getUnit(), ingredient.getDefaultUnit())) {
            throw new InvalidParLevelException(
                    "unit '" + parLevel.getUnit() + "' is incompatible with ingredient defaultUnit '"
                            + ingredient.getDefaultUnit() + "'");
        }
    }
}
