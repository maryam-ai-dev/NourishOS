package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nourishos.authority.domain.LotAllocation;
import com.nourishos.authority.repository.IngredientLotRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LotAllocationService {

    private final IngredientLotRepository lotRepository;
    private final UnitConversionService unitConversionService;

    public List<LotAllocation> allocate(UUID ingredientId, UUID householdId,
                                         BigDecimal requiredQuantity, String unit) {
        // Stub — full implementation in Sprint 3.10 and 3.11
        return new ArrayList<>();
    }
}
