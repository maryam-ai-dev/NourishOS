package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IngredientLotTest {

    @Test
    void expiryBeforePurchaseThrowsWithoutCorrectionFlag() {
        IngredientLot lot = new IngredientLot();
        lot.setPurchasedAt(Instant.now());
        lot.setExpiryDate(Instant.now().minus(1, ChronoUnit.DAYS));
        lot.setCorrectionFlag(false);

        assertThrows(IllegalStateException.class, lot::validateExpiryDate);
    }

    @Test
    void expiryBeforePurchaseAllowedWithCorrectionFlag() {
        IngredientLot lot = new IngredientLot();
        lot.setPurchasedAt(Instant.now());
        lot.setExpiryDate(Instant.now().minus(1, ChronoUnit.DAYS));
        lot.setCorrectionFlag(true);

        assertDoesNotThrow(lot::validateExpiryDate);
    }

    @Test
    void expiryAfterPurchaseAlwaysAllowed() {
        IngredientLot lot = new IngredientLot();
        lot.setPurchasedAt(Instant.now());
        lot.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        lot.setCorrectionFlag(false);

        assertDoesNotThrow(lot::validateExpiryDate);
    }

    @Test
    void nullExpiryDateAllowed() {
        IngredientLot lot = new IngredientLot();
        lot.setPurchasedAt(Instant.now());
        lot.setExpiryDate(null);

        assertDoesNotThrow(lot::validateExpiryDate);
    }

    @Test
    void defaultStatusIsActive() {
        IngredientLot lot = new IngredientLot();
        assertEquals(LotStatus.ACTIVE, lot.getStatus());
    }
}
