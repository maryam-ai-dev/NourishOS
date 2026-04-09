package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnitConversionServiceTest {

    private UnitConversionService service;

    @BeforeEach
    void setUp() {
        service = new UnitConversionService();
    }

    @Test
    void convertGramsToKilograms() {
        BigDecimal result = service.convert(500, "g", "kg");
        assertEquals(0, new BigDecimal("0.5").compareTo(result));
    }

    @Test
    void convertKilogramsToGrams() {
        BigDecimal result = service.convert(1, "kg", "g");
        assertEquals(0, new BigDecimal("1000").compareTo(result));
    }

    @Test
    void convertLitresToMillilitres() {
        BigDecimal result = service.convert(1, "l", "ml");
        assertEquals(0, new BigDecimal("1000").compareTo(result));
    }

    @Test
    void convertMillilitresToLitres() {
        BigDecimal result = service.convert(250, "ml", "l");
        assertEquals(0, new BigDecimal("0.25").compareTo(result));
    }

    @Test
    void sameUnitReturnsOriginal() {
        BigDecimal result = service.convert(42, "g", "g");
        assertEquals(0, new BigDecimal("42").compareTo(result));
    }

    @Test
    void unitToUnitReturnsOriginal() {
        BigDecimal result = service.convert(6, "unit", "unit");
        assertEquals(0, new BigDecimal("6").compareTo(result));
    }

    @Test
    void crossCategoryThrowsIncompatibleUnitsException() {
        assertThrows(IncompatibleUnitsException.class,
                () -> service.convert(500, "g", "ml"));
    }

    @Test
    void crossCategoryMassToCountableThrows() {
        assertThrows(IncompatibleUnitsException.class,
                () -> service.convert(1, "kg", "unit"));
    }

    @Test
    void crossCategoryVolumeToCountableThrows() {
        assertThrows(IncompatibleUnitsException.class,
                () -> service.convert(100, "ml", "unit"));
    }

    @Test
    void unknownUnitThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.convert(1, "oz", "g"));
    }

    @Test
    void areCompatibleSameCategory() {
        assertTrue(service.areCompatible("g", "kg"));
        assertTrue(service.areCompatible("ml", "l"));
    }

    @Test
    void areCompatibleDifferentCategory() {
        assertFalse(service.areCompatible("g", "ml"));
        assertFalse(service.areCompatible("unit", "kg"));
    }
}
