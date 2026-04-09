package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nourishos.authority.domain.AdjustmentType;
import com.nourishos.authority.domain.IngredientLot;
import com.nourishos.authority.domain.InventoryAdjustment;
import com.nourishos.authority.repository.IngredientLotRepository;
import com.nourishos.authority.repository.InventoryAdjustmentRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryAdjustmentServiceTest {

    @Mock
    private InventoryAdjustmentRepository adjustmentRepository;

    @Mock
    private IngredientLotRepository lotRepository;

    @InjectMocks
    private InventoryAdjustmentService service;

    private UUID lotId;
    private IngredientLot lot;

    @BeforeEach
    void setUp() {
        lotId = UUID.randomUUID();
        lot = new IngredientLot();
        lot.setId(lotId);
        lot.setQuantity(new BigDecimal("500"));
        lot.setUnit("g");
    }

    @Test
    void purchaseIncreasesQuantity() {
        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));
        when(lotRepository.save(any())).thenReturn(lot);
        when(adjustmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.record(lotId, AdjustmentType.PURCHASE, new BigDecimal("200"), "g", "Restocked");

        assertEquals(0, new BigDecimal("700").compareTo(lot.getQuantity()));
        verify(adjustmentRepository).save(any(InventoryAdjustment.class));
    }

    @Test
    void consumptionDecreasesQuantity() {
        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));
        when(lotRepository.save(any())).thenReturn(lot);
        when(adjustmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.record(lotId, AdjustmentType.CONSUMPTION, new BigDecimal("-100"), "g", "Used in recipe");

        assertEquals(0, new BigDecimal("400").compareTo(lot.getQuantity()));
    }

    @Test
    void negativeResultThrowsException() {
        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));

        assertThrows(NegativeQuantityException.class,
                () -> service.record(lotId, AdjustmentType.DEDUCTION, new BigDecimal("-600"), "g", "Over-deduction"));

        verify(lotRepository, never()).save(any());
        verify(adjustmentRepository, never()).save(any());
    }

    @Test
    void exactZeroAllowed() {
        when(lotRepository.findById(lotId)).thenReturn(Optional.of(lot));
        when(lotRepository.save(any())).thenReturn(lot);
        when(adjustmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(
                () -> service.record(lotId, AdjustmentType.CONSUMPTION, new BigDecimal("-500"), "g", "Used all"));

        assertEquals(0, BigDecimal.ZERO.compareTo(lot.getQuantity()));
    }
}
