package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.ConsumptionEvent;
import com.nourishos.authority.domain.FoodFlowSnapshot;
import com.nourishos.authority.domain.WasteEvent;
import com.nourishos.authority.repository.ConsumptionEventRepository;
import com.nourishos.authority.repository.FoodFlowSnapshotRepository;
import com.nourishos.authority.repository.WasteEventRepository;
import com.nourishos.authority.service.HouseholdService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodFlowSnapshotService {

    private final FoodFlowSnapshotRepository snapshotRepository;
    private final ConsumptionEventRepository consumptionEventRepository;
    private final WasteEventRepository wasteEventRepository;
    private final HouseholdService householdService;
    private final ObjectMapper objectMapper;

    @Transactional
    public FoodFlowSnapshot generate(UUID householdId) {
        var household = householdService.findById(householdId);
        Instant cutoff = Instant.now().minus(28, ChronoUnit.DAYS);

        List<ConsumptionEvent> consumptions = consumptionEventRepository.findByHouseholdId(householdId).stream()
                .filter(e -> e.getConsumedAt().isAfter(cutoff))
                .toList();

        List<WasteEvent> wastes = wasteEventRepository.findByHouseholdId(householdId).stream()
                .filter(e -> e.getWastedAt().isAfter(cutoff))
                .toList();

        BigDecimal totalConsumed = consumptions.stream()
                .map(ConsumptionEvent::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWaste = wastes.stream()
                .map(WasteEvent::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal denominator = totalWaste.add(totalConsumed);
        BigDecimal wasteRatio = denominator.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalWaste.divide(denominator, 4, RoundingMode.HALF_UP);

        String topConsumed = topIngredients(consumptions.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getIngredient().getId(),
                        Collectors.reducing(BigDecimal.ZERO, ConsumptionEvent::getQuantity, BigDecimal::add))));

        String topWasted = topIngredients(wastes.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getIngredient().getId(),
                        Collectors.reducing(BigDecimal.ZERO, WasteEvent::getQuantity, BigDecimal::add))));

        FoodFlowSnapshot snapshot = snapshotRepository
                .findFirstByHouseholdIdOrderBySnapshotDateDesc(householdId)
                .orElseGet(() -> {
                    FoodFlowSnapshot s = new FoodFlowSnapshot();
                    s.setHousehold(household);
                    return s;
                });

        snapshot.setSnapshotDate(LocalDate.now());
        snapshot.setTotalConsumedGrams(totalConsumed);
        snapshot.setTotalWasteGrams(totalWaste);
        snapshot.setWasteRatio(wasteRatio);
        snapshot.setTopConsumedIngredients(topConsumed);
        snapshot.setTopWastedIngredients(topWasted);

        return snapshotRepository.save(snapshot);
    }

    @Transactional(readOnly = true)
    public FoodFlowSnapshot getLatest(UUID householdId) {
        return snapshotRepository.findFirstByHouseholdIdOrderBySnapshotDateDesc(householdId)
                .orElseThrow(() -> new IllegalArgumentException("No snapshot for household: " + householdId));
    }

    private String topIngredients(Map<UUID, BigDecimal> grouped) {
        var top = grouped.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> Map.of("ingredientId", e.getKey().toString(), "quantity", e.getValue()))
                .toList();
        try {
            return objectMapper.writeValueAsString(top);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
