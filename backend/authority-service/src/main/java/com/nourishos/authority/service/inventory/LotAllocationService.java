package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nourishos.authority.domain.IngredientLot;
import com.nourishos.authority.domain.LotAllocation;
import com.nourishos.authority.domain.LotStatus;
import com.nourishos.authority.repository.IngredientLotRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LotAllocationService {

    private final IngredientLotRepository lotRepository;
    private final UnitConversionService unitConversionService;

    public List<LotAllocation> allocate(UUID ingredientId, UUID householdId,
                                         BigDecimal requiredQuantity, String unit) {
        List<IngredientLot> activeLots = new ArrayList<>(
                lotRepository.findByIngredientIdAndStatus(ingredientId, LotStatus.ACTIVE));

        // Sort: opened first, then by nearest expiry
        activeLots.sort(openedFirstThenNearestExpiry());

        List<LotAllocation> allocations = new ArrayList<>();
        BigDecimal remaining = requiredQuantity;

        for (IngredientLot lot : activeLots) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal available = convertToRequestedUnit(lot, unit);
            if (available.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal take = available.min(remaining);
            allocations.add(new LotAllocation(lot.getId(), take, unit));
            remaining = remaining.subtract(take);
        }

        return allocations;
    }

    private BigDecimal convertToRequestedUnit(IngredientLot lot, String targetUnit) {
        if (lot.getUnit().equals(targetUnit)) {
            return lot.getQuantity();
        }
        return unitConversionService.convert(lot.getQuantity(), lot.getUnit(), targetUnit);
    }

    static Comparator<IngredientLot> openedFirstThenNearestExpiry() {
        return Comparator
                .comparing((IngredientLot lot) -> !lot.isOpen())  // opened first (false < true)
                .thenComparing(lot -> lot.getExpiryDate() == null
                        ? java.time.Instant.MAX
                        : lot.getExpiryDate());
    }
}
