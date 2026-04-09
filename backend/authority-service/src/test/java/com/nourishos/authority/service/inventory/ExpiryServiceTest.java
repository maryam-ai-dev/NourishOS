package com.nourishos.authority.service.inventory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nourishos.authority.domain.ExpiryRecord;
import com.nourishos.authority.domain.FreshnessStatus;
import com.nourishos.authority.domain.IngredientLot;
import com.nourishos.authority.domain.LotStatus;
import com.nourishos.authority.repository.ExpiryRecordRepository;
import com.nourishos.authority.repository.IngredientLotRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpiryServiceTest {

    @Mock
    private IngredientLotRepository lotRepository;

    @Mock
    private ExpiryRecordRepository expiryRecordRepository;

    @InjectMocks
    private ExpiryService service;

    private IngredientLot lotWithExpiry(int daysFromNow) {
        IngredientLot lot = new IngredientLot();
        lot.setId(UUID.randomUUID());
        lot.setStatus(LotStatus.ACTIVE);
        lot.setExpiryDate(Instant.now().plus(daysFromNow, ChronoUnit.DAYS));
        return lot;
    }

    @Test
    void lotExpiringIn2DaysSetToNearExpiry() {
        IngredientLot lot = lotWithExpiry(2);
        when(lotRepository.findByStatus(LotStatus.ACTIVE)).thenReturn(List.of(lot));
        when(expiryRecordRepository.findByLotId(lot.getId())).thenReturn(Optional.empty());
        when(expiryRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.flagNearExpiry();

        ArgumentCaptor<ExpiryRecord> captor = ArgumentCaptor.forClass(ExpiryRecord.class);
        verify(expiryRecordRepository).save(captor.capture());
        assertEquals(FreshnessStatus.NEAR_EXPIRY, captor.getValue().getFreshnessStatus());
        assertNotNull(captor.getValue().getNotifiedAt());
    }

    @Test
    void lotExpiringIn10DaysStaysFresh() {
        IngredientLot lot = lotWithExpiry(10);
        when(lotRepository.findByStatus(LotStatus.ACTIVE)).thenReturn(List.of(lot));
        when(expiryRecordRepository.findByLotId(lot.getId())).thenReturn(Optional.empty());
        when(expiryRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.flagNearExpiry();

        ArgumentCaptor<ExpiryRecord> captor = ArgumentCaptor.forClass(ExpiryRecord.class);
        verify(expiryRecordRepository).save(captor.capture());
        assertEquals(FreshnessStatus.FRESH, captor.getValue().getFreshnessStatus());
        assertNull(captor.getValue().getNotifiedAt());
    }

    @Test
    void alreadyExpiredLotSetToExpired() {
        IngredientLot lot = lotWithExpiry(-1);
        when(lotRepository.findByStatus(LotStatus.ACTIVE)).thenReturn(List.of(lot));
        when(expiryRecordRepository.findByLotId(lot.getId())).thenReturn(Optional.empty());
        when(expiryRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.flagNearExpiry();

        ArgumentCaptor<ExpiryRecord> captor = ArgumentCaptor.forClass(ExpiryRecord.class);
        verify(expiryRecordRepository).save(captor.capture());
        assertEquals(FreshnessStatus.EXPIRED, captor.getValue().getFreshnessStatus());
        assertNotNull(captor.getValue().getNotifiedAt());
    }

    @Test
    void callingTwiceDoesNotUpdateNotifiedAtIfStatusUnchanged() {
        IngredientLot lot = lotWithExpiry(2);
        Instant firstNotified = Instant.now().minus(1, ChronoUnit.HOURS);

        ExpiryRecord existing = new ExpiryRecord();
        existing.setId(UUID.randomUUID());
        existing.setLot(lot);
        existing.setExpiryDate(lot.getExpiryDate());
        existing.setFreshnessStatus(FreshnessStatus.NEAR_EXPIRY);
        existing.setNotifiedAt(firstNotified);

        when(lotRepository.findByStatus(LotStatus.ACTIVE)).thenReturn(List.of(lot));
        when(expiryRecordRepository.findByLotId(lot.getId())).thenReturn(Optional.of(existing));
        when(expiryRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.flagNearExpiry();

        ArgumentCaptor<ExpiryRecord> captor = ArgumentCaptor.forClass(ExpiryRecord.class);
        verify(expiryRecordRepository).save(captor.capture());
        assertEquals(FreshnessStatus.NEAR_EXPIRY, captor.getValue().getFreshnessStatus());
        assertEquals(firstNotified, captor.getValue().getNotifiedAt());
    }

    @Test
    void lotWithNullExpiryDateIsSkipped() {
        IngredientLot lot = new IngredientLot();
        lot.setId(UUID.randomUUID());
        lot.setStatus(LotStatus.ACTIVE);
        lot.setExpiryDate(null);

        when(lotRepository.findByStatus(LotStatus.ACTIVE)).thenReturn(List.of(lot));

        service.flagNearExpiry();

        verify(expiryRecordRepository, never()).save(any());
    }
}
