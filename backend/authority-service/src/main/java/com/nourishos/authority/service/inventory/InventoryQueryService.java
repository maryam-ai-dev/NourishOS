package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nourishos.authority.domain.IngredientLot;
import com.nourishos.authority.domain.LotStatus;
import com.nourishos.authority.domain.ParLevel;
import com.nourishos.authority.domain.StorageLocation;
import com.nourishos.authority.repository.IngredientLotRepository;
import com.nourishos.authority.repository.ParLevelRepository;
import com.nourishos.authority.repository.StorageLocationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryQueryService {

    private final IngredientLotRepository lotRepository;
    private final StorageLocationRepository locationRepository;
    private final ParLevelRepository parLevelRepository;
    private final UnitConversionService unitConversionService;

    @Transactional(readOnly = true)
    public List<IngredientLot> getActiveLots() {
        return lotRepository.findByStatus(LotStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<IngredientLot> getExpiringLots() {
        Instant threshold = Instant.now().plus(3, ChronoUnit.DAYS);
        return lotRepository.findByStatus(LotStatus.ACTIVE).stream()
                .filter(lot -> lot.getExpiryDate() != null && lot.getExpiryDate().isBefore(threshold))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LowStockItem> getLowStockItems(UUID householdId) {
        List<ParLevel> parLevels = parLevelRepository.findByHouseholdId(householdId);
        List<LowStockItem> lowStock = new ArrayList<>();

        for (ParLevel pl : parLevels) {
            List<IngredientLot> lots = lotRepository.findByIngredientIdAndStatus(
                    pl.getIngredient().getId(), LotStatus.ACTIVE);

            BigDecimal totalQuantity = lots.stream()
                    .map(lot -> {
                        if (lot.getUnit().equals(pl.getUnit())) {
                            return lot.getQuantity();
                        }
                        return unitConversionService.convert(lot.getQuantity(), lot.getUnit(), pl.getUnit());
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalQuantity.compareTo(pl.getMinimumQuantity()) < 0) {
                lowStock.add(new LowStockItem(
                        pl.getIngredient().getId(),
                        pl.getIngredient().getName(),
                        totalQuantity,
                        pl.getMinimumQuantity(),
                        pl.getPreferredQuantity(),
                        pl.getUnit()));
            }
        }

        return lowStock;
    }

    @Transactional(readOnly = true)
    public List<StorageLocation> getLocations(UUID householdId) {
        return locationRepository.findByHouseholdId(householdId);
    }

    public record LowStockItem(
            UUID ingredientId,
            String ingredientName,
            BigDecimal currentQuantity,
            BigDecimal minimumQuantity,
            BigDecimal preferredQuantity,
            String unit
    ) {}
}
