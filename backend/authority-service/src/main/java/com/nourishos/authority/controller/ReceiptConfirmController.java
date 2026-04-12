package com.nourishos.authority.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.AdjustmentType;
import com.nourishos.authority.domain.Ingredient;
import com.nourishos.authority.domain.IngredientLot;
import com.nourishos.authority.domain.InventoryAdjustment;
import com.nourishos.authority.domain.LotStatus;
import com.nourishos.authority.domain.PerishabilityClass;
import com.nourishos.authority.repository.IngredientLotRepository;
import com.nourishos.authority.repository.IngredientRepository;
import com.nourishos.authority.repository.InventoryAdjustmentRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pantry/receipt")
@RequiredArgsConstructor
public class ReceiptConfirmController {

    private final IngredientRepository ingredientRepository;
    private final IngredientLotRepository lotRepository;
    private final InventoryAdjustmentRepository adjustmentRepository;

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String, Object> confirm(@RequestBody ConfirmRequest request) {
        int created = 0;
        for (ConfirmedItem item : request.items) {
            // Fuzzy name match — case-insensitive exact match first
            Ingredient ingredient = ingredientRepository.findAll().stream()
                    .filter(i -> i.getName().equalsIgnoreCase(item.name.trim()))
                    .findFirst()
                    .orElseGet(() -> {
                        Ingredient newIng = new Ingredient();
                        newIng.setName(item.name.trim());
                        newIng.setCategory("OTHER");
                        newIng.setDefaultUnit(item.unit != null ? item.unit : "unit");
                        newIng.setPerishabilityClass(PerishabilityClass.PERISHABLE);
                        if (item.estimatedCostPerUnit != null) {
                            newIng.setEstimatedCostPerUnit(BigDecimal.valueOf(item.estimatedCostPerUnit));
                        }
                        return ingredientRepository.save(newIng);
                    });

            // Create lot
            IngredientLot lot = new IngredientLot();
            lot.setIngredient(ingredient);
            lot.setQuantity(BigDecimal.valueOf(item.quantity));
            lot.setUnit(item.unit);
            lot.setStatus(LotStatus.ACTIVE);
            lot.setPurchasedAt(Instant.now());
            IngredientLot savedLot = lotRepository.save(lot);

            // Create PURCHASE adjustment
            InventoryAdjustment adj = new InventoryAdjustment();
            adj.setLot(savedLot);
            adj.setAdjustmentType(AdjustmentType.PURCHASE);
            adj.setQuantityDelta(BigDecimal.valueOf(item.quantity));
            adj.setUnit(item.unit);
            adj.setReason("Receipt confirm");
            adjustmentRepository.save(adj);

            created++;
        }
        return Map.of("lotsCreated", created);
    }

    public static class ConfirmRequest {
        public UUID householdId;
        public List<ConfirmedItem> items;
    }

    public static class ConfirmedItem {
        public String name;
        public double quantity;
        public String unit;
        public Double estimatedCostPerUnit;
    }
}
