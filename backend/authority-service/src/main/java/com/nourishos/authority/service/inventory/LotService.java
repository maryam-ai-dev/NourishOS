package com.nourishos.authority.service.inventory;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nourishos.authority.domain.AdjustmentType;
import com.nourishos.authority.domain.Ingredient;
import com.nourishos.authority.domain.IngredientLot;
import com.nourishos.authority.domain.StorageLocation;
import com.nourishos.authority.dto.CreateLotRequest;
import com.nourishos.authority.repository.IngredientLotRepository;
import com.nourishos.authority.repository.StorageLocationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LotService {

    private final IngredientLotRepository lotRepository;
    private final StorageLocationRepository locationRepository;
    private final IngredientService ingredientService;
    private final InventoryAdjustmentService adjustmentService;

    @Transactional
    public IngredientLot addLot(CreateLotRequest request) {
        Ingredient ingredient = ingredientService.findById(request.getIngredientId());

        StorageLocation location = null;
        if (request.getStorageLocationId() != null) {
            location = locationRepository.findById(request.getStorageLocationId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Storage location not found: " + request.getStorageLocationId()));
        }

        IngredientLot lot = new IngredientLot();
        lot.setIngredient(ingredient);
        lot.setStorageLocation(location);
        lot.setQuantity(request.getQuantity());
        lot.setUnit(request.getUnit());
        lot.setPurchasedAt(Instant.now());
        lot.setExpiryDate(request.getExpiryDate());

        IngredientLot saved = lotRepository.save(lot);

        adjustmentService.record(
                saved.getId(),
                AdjustmentType.PURCHASE,
                request.getQuantity(),
                request.getUnit(),
                "Initial purchase");

        return saved;
    }
}
