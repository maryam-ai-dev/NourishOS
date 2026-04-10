package com.nourishos.authority.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.IngredientLot;
import com.nourishos.authority.domain.StockSnapshot;
import com.nourishos.authority.domain.StorageLocation;
import com.nourishos.authority.service.inventory.InventoryQueryService;
import com.nourishos.authority.service.inventory.SnapshotService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final SnapshotService snapshotService;
    private final InventoryQueryService inventoryQueryService;

    @GetMapping
    public List<IngredientLot> activeLots() {
        return inventoryQueryService.getActiveLots();
    }

    @GetMapping("/expiring")
    public List<IngredientLot> expiringLots() {
        return inventoryQueryService.getExpiringLots();
    }

    @GetMapping("/low-stock")
    public List<InventoryQueryService.LowStockItem> lowStock(@RequestParam UUID householdId) {
        return inventoryQueryService.getLowStockItems(householdId);
    }

    @GetMapping("/locations")
    public List<StorageLocation> locations(@RequestParam UUID householdId) {
        return inventoryQueryService.getLocations(householdId);
    }

    @GetMapping("/snapshot")
    public StockSnapshot snapshot(@RequestParam UUID householdId) {
        return snapshotService.generateSnapshot(householdId);
    }
}
