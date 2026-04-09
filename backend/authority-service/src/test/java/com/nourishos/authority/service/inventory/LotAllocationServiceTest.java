package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nourishos.authority.domain.LotAllocation;
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

    @Test
    void stubReturnsEmptyListWithoutThrowing() {
        List<LotAllocation> result = service.allocate(
                UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100"), "g");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void returnTypeIsTypedList() {
        List<LotAllocation> result = service.allocate(
                UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("50"), "ml");

        // Compile-time: return type is List<LotAllocation>, not raw List
        assertInstanceOf(List.class, result);
    }

    @Test
    void stubMakesZeroRepositoryCalls() {
        service.allocate(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100"), "g");

        verifyNoInteractions(lotRepository);
    }
}
