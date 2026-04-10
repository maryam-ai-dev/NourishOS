package com.nourishos.authority.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.FoodFlowSnapshot;
import com.nourishos.authority.service.inventory.FoodFlowSnapshotService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/foodflow")
@RequiredArgsConstructor
public class FoodFlowController {

    private final FoodFlowSnapshotService snapshotService;

    @GetMapping("/snapshot")
    public FoodFlowSnapshot snapshot(@RequestParam UUID householdId) {
        return snapshotService.getLatest(householdId);
    }

    @GetMapping("/snapshot/refresh")
    public FoodFlowSnapshot refresh(@RequestParam UUID householdId) {
        return snapshotService.generate(householdId);
    }
}
