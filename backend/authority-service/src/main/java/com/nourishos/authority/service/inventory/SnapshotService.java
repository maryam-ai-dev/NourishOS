package com.nourishos.authority.service.inventory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nourishos.authority.domain.IngredientLot;
import com.nourishos.authority.domain.LotStatus;
import com.nourishos.authority.domain.StockSnapshot;
import com.nourishos.authority.domain.StorageLocation;
import com.nourishos.authority.repository.IngredientLotRepository;
import com.nourishos.authority.repository.StorageLocationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final StorageLocationRepository locationRepository;
    private final IngredientLotRepository lotRepository;

    @Transactional(readOnly = true)
    public StockSnapshot generateSnapshot(UUID householdId) {
        List<StorageLocation> locations = locationRepository.findByHouseholdId(householdId);

        Map<UUID, StockSnapshot.LocationGroup> grouped = new LinkedHashMap<>();

        for (StorageLocation loc : locations) {
            List<IngredientLot> activeLots =
                    lotRepository.findByStorageLocationIdAndStatus(loc.getId(), LotStatus.ACTIVE);

            List<StockSnapshot.LotSummary> summaries = activeLots.stream()
                    .map(lot -> new StockSnapshot.LotSummary(
                            lot.getId(),
                            lot.getIngredient().getId(),
                            lot.getIngredient().getName(),
                            lot.getQuantity(),
                            lot.getUnit(),
                            lot.isOpen(),
                            lot.getExpiryDate()))
                    .toList();

            grouped.put(loc.getId(), new StockSnapshot.LocationGroup(
                    loc.getId(),
                    loc.getLocationType().name(),
                    loc.getLabel(),
                    summaries));
        }

        StockSnapshot snapshot = new StockSnapshot();
        snapshot.setHouseholdId(householdId);
        snapshot.setGeneratedAt(Instant.now());
        snapshot.setLocations(new ArrayList<>(grouped.values()));
        return snapshot;
    }
}
