package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nourishos.authority.domain.AdjustmentType;
import com.nourishos.authority.domain.IngredientLot;
import com.nourishos.authority.domain.InventoryAdjustment;
import com.nourishos.authority.repository.IngredientLotRepository;
import com.nourishos.authority.repository.InventoryAdjustmentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryAdjustmentService {

    private final InventoryAdjustmentRepository adjustmentRepository;
    private final IngredientLotRepository lotRepository;

    @Transactional
    public InventoryAdjustment record(UUID lotId, AdjustmentType type,
                                       BigDecimal quantityDelta, String unit, String reason) {
        IngredientLot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("Lot not found: " + lotId));

        BigDecimal newQuantity = lot.getQuantity().add(quantityDelta);
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeQuantityException(lotId, lot.getQuantity(), quantityDelta);
        }

        lot.setQuantity(newQuantity);
        lotRepository.save(lot);

        InventoryAdjustment adjustment = new InventoryAdjustment();
        adjustment.setLot(lot);
        adjustment.setAdjustmentType(type);
        adjustment.setQuantityDelta(quantityDelta);
        adjustment.setUnit(unit);
        adjustment.setReason(reason);
        return adjustmentRepository.save(adjustment);
    }
}
