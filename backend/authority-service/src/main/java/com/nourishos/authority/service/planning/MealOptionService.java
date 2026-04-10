package com.nourishos.authority.service.planning;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nourishos.authority.domain.MealOption;
import com.nourishos.authority.repository.MealOptionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MealOptionService {

    private final MealOptionRepository mealOptionRepository;

    public MealOption create(MealOption mealOption) {
        validateSustainabilityScore(mealOption.getSustainabilityScore());
        return mealOptionRepository.save(mealOption);
    }

    public MealOption findById(UUID id) {
        return mealOptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MealOption not found: " + id));
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
        return mealOptionRepository.save(existing);
    }

    private void validateSustainabilityScore(BigDecimal score) {
        if (score != null) {
            if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.ONE) > 0) {
                throw new InvalidSustainabilityScoreException(score);
            }
        }
    }
}
