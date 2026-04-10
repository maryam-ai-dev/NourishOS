package com.nourishos.authority.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.IngredientLot;
import com.nourishos.authority.domain.StockSnapshot;
import com.nourishos.authority.domain.StorageLocation;
import com.nourishos.authority.dto.CreateLotRequest;
import com.nourishos.authority.dto.UpdateLotQuantityRequest;
import com.nourishos.authority.service.inventory.InventoryQueryService;
import com.nourishos.authority.service.inventory.LotService;
import com.nourishos.authority.service.inventory.SnapshotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final SnapshotService snapshotService;
    private final InventoryQueryService inventoryQueryService;
    private final LotService lotService;

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

    @PostMapping("/lots")
    @ResponseStatus(HttpStatus.CREATED)
    public IngredientLot addLot(@Valid @RequestBody CreateLotRequest request) {
        return lotService.addLot(request);
    }

    @PutMapping("/lots/{id}")
    public IngredientLot updateLotQuantity(@PathVariable UUID id,
                                           @Valid @RequestBody UpdateLotQuantityRequest request) {
        return lotService.updateQuantity(id, request.getNewQuantity(), request.getReason());
    }

    @GetMapping("/snapshot")
    public StockSnapshot snapshot(@RequestParam UUID householdId) {
        return snapshotService.generateSnapshot(householdId);
    }
}
