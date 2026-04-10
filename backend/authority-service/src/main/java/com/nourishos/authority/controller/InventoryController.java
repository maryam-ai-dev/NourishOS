package com.nourishos.authority.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.StockSnapshot;
import com.nourishos.authority.service.inventory.SnapshotService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final SnapshotService snapshotService;

    @GetMapping("/snapshot")
    public StockSnapshot snapshot(@RequestParam UUID householdId) {
        return snapshotService.generateSnapshot(householdId);
    }
}
