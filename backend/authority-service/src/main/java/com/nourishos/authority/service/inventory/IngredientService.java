package com.nourishos.authority.service.inventory;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nourishos.authority.domain.Ingredient;
import com.nourishos.authority.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private static final Set<String> CANONICAL_UNITS = Set.of("g", "ml", "unit");

    private final IngredientRepository ingredientRepository;

    public Ingredient create(Ingredient ingredient) {
        validateCanonicalUnit(ingredient.getDefaultUnit());
        return ingredientRepository.save(ingredient);
    }

    public Ingredient findById(UUID id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new IngredientNotFoundException(id));
    }

    private void validateCanonicalUnit(String unit) {
        if (!CANONICAL_UNITS.contains(unit)) {
            throw new InvalidCanonicalUnitException(unit);
        }
    }
}
