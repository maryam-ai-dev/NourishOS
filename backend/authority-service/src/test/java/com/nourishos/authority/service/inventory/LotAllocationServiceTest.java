package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nourishos.authority.domain.IngredientLot;
import com.nourishos.authority.domain.LotAllocation;
import com.nourishos.authority.domain.LotStatus;
import com.nourishos.authority.repository.IngredientLotRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotAllocationServiceTest {

    @Mock
    private IngredientLotRepository lotRepository;

    @Mock
    private UnitConversionService unitConversionService;

    @InjectMocks
    private LotAllocationService service;

    private IngredientLot makeLot(BigDecimal qty, String unit, boolean open, Instant expiry) {
        IngredientLot lot = new IngredientLot();
        lot.setId(UUID.randomUUID());
        lot.setQuantity(qty);
        lot.setUnit(unit);
        lot.setOpen(open);
        lot.setExpiryDate(expiry);
        lot.setStatus(LotStatus.ACTIVE);
        return lot;
    }

    @Test
    void noLotsReturnsEmptyList() {
        UUID ingredientId = UUID.randomUUID();
        when(lotRepository.findByIngredientIdAndStatus(ingredientId, LotStatus.ACTIVE))
                .thenReturn(List.of());

        List<LotAllocation> result = service.allocate(
                ingredientId, UUID.randomUUID(), new BigDecimal("100"), "g");

        assertTrue(result.isEmpty());
    }

    @Test
    void openedLotSelectedBeforeSealedLotRegardlessOfExpiry() {
        UUID ingredientId = UUID.randomUUID();
        // Sealed lot expires sooner, opened lot expires later
        IngredientLot sealed = makeLot(new BigDecimal("300"), "g", false,
                Instant.now().plus(2, ChronoUnit.DAYS));
        IngredientLot opened = makeLot(new BigDecimal("300"), "g", true,
                Instant.now().plus(10, ChronoUnit.DAYS));

        when(lotRepository.findByIngredientIdAndStatus(ingredientId, LotStatus.ACTIVE))
                .thenReturn(List.of(sealed, opened));

        List<LotAllocation> result = service.allocate(
                ingredientId, UUID.randomUUID(), new BigDecimal("200"), "g");

        assertEquals(1, result.size());
        assertEquals(opened.getId(), result.get(0).getLotId());
        assertEquals(0, new BigDecimal("200").compareTo(result.get(0).getQuantityFromThisLot()));
    }

    @Test
    void allocationQuantitiesSumToRequired() {
        UUID ingredientId = UUID.randomUUID();
        IngredientLot opened = makeLot(new BigDecimal("150"), "g", true,
                Instant.now().plus(5, ChronoUnit.DAYS));
        IngredientLot sealed = makeLot(new BigDecimal("300"), "g", false,
                Instant.now().plus(3, ChronoUnit.DAYS));

        when(lotRepository.findByIngredientIdAndStatus(ingredientId, LotStatus.ACTIVE))
                .thenReturn(List.of(sealed, opened));

        BigDecimal required = new BigDecimal("200");
        List<LotAllocation> result = service.allocate(
                ingredientId, UUID.randomUUID(), required, "g");

        BigDecimal totalAllocated = result.stream()
                .map(LotAllocation::getQuantityFromThisLot)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, required.compareTo(totalAllocated));
    }

    @Test
    void openedLotFullyUsedThenSealedLotFillsRemainder() {
        UUID ingredientId = UUID.randomUUID();
        IngredientLot opened = makeLot(new BigDecimal("100"), "g", true,
                Instant.now().plus(5, ChronoUnit.DAYS));
        IngredientLot sealed = makeLot(new BigDecimal("400"), "g", false,
                Instant.now().plus(3, ChronoUnit.DAYS));

        when(lotRepository.findByIngredientIdAndStatus(ingredientId, LotStatus.ACTIVE))
                .thenReturn(List.of(sealed, opened));

        List<LotAllocation> result = service.allocate(
                ingredientId, UUID.randomUUID(), new BigDecimal("250"), "g");

        assertEquals(2, result.size());
        // First allocation: opened lot, takes all 100
        assertEquals(opened.getId(), result.get(0).getLotId());
        assertEquals(0, new BigDecimal("100").compareTo(result.get(0).getQuantityFromThisLot()));
        // Second allocation: sealed lot, takes remaining 150
        assertEquals(sealed.getId(), result.get(1).getLotId());
        assertEquals(0, new BigDecimal("150").compareTo(result.get(1).getQuantityFromThisLot()));
    }

    @Test
    void nearerExpirySealedLotSelectedBeforeLaterExpiry() {
        UUID ingredientId = UUID.randomUUID();
        IngredientLot laterExpiry = makeLot(new BigDecimal("500"), "g", false,
                Instant.now().plus(10, ChronoUnit.DAYS));
        IngredientLot nearerExpiry = makeLot(new BigDecimal("500"), "g", false,
                Instant.now().plus(2, ChronoUnit.DAYS));

        when(lotRepository.findByIngredientIdAndStatus(ingredientId, LotStatus.ACTIVE))
                .thenReturn(List.of(laterExpiry, nearerExpiry));

        List<LotAllocation> result = service.allocate(
                ingredientId, UUID.randomUUID(), new BigDecimal("200"), "g");

        assertEquals(1, result.size());
        assertEquals(nearerExpiry.getId(), result.get(0).getLotId());
        assertEquals(0, new BigDecimal("200").compareTo(result.get(0).getQuantityFromThisLot()));
    }

    @Test
    void partialAllocationWhenInsufficientStock() {
        UUID ingredientId = UUID.randomUUID();
        IngredientLot lot1 = makeLot(new BigDecimal("80"), "g", false,
                Instant.now().plus(3, ChronoUnit.DAYS));
        IngredientLot lot2 = makeLot(new BigDecimal("50"), "g", false,
                Instant.now().plus(5, ChronoUnit.DAYS));

        when(lotRepository.findByIngredientIdAndStatus(ingredientId, LotStatus.ACTIVE))
                .thenReturn(List.of(lot1, lot2));

        List<LotAllocation> result = service.allocate(
                ingredientId, UUID.randomUUID(), new BigDecimal("200"), "g");

        // Should not crash — returns partial allocation
        assertEquals(2, result.size());
        BigDecimal totalAllocated = result.stream()
                .map(LotAllocation::getQuantityFromThisLot)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("130").compareTo(totalAllocated));
        // Total < required, but no exception
    }

    @Test
    void nullExpiryDateSortedAfterLotsWithExpiry() {
        UUID ingredientId = UUID.randomUUID();
        IngredientLot withExpiry = makeLot(new BigDecimal("500"), "g", false,
                Instant.now().plus(5, ChronoUnit.DAYS));
        IngredientLot noExpiry = makeLot(new BigDecimal("500"), "g", false, null);

        when(lotRepository.findByIngredientIdAndStatus(ingredientId, LotStatus.ACTIVE))
                .thenReturn(List.of(noExpiry, withExpiry));

        List<LotAllocation> result = service.allocate(
                ingredientId, UUID.randomUUID(), new BigDecimal("100"), "g");

        assertEquals(1, result.size());
        assertEquals(withExpiry.getId(), result.get(0).getLotId());
    }
}
