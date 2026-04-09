package com.nourishos.authority.service.inventory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nourishos.authority.domain.ExpiryRecord;
import com.nourishos.authority.domain.FreshnessStatus;
import com.nourishos.authority.domain.IngredientLot;
import com.nourishos.authority.domain.LotStatus;
import com.nourishos.authority.repository.ExpiryRecordRepository;
import com.nourishos.authority.repository.IngredientLotRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpiryService {

    private static final int NEAR_EXPIRY_DAYS = 3;

    private final IngredientLotRepository lotRepository;
    private final ExpiryRecordRepository expiryRecordRepository;

    @Transactional
    public void flagNearExpiry() {
        Instant now = Instant.now();
        List<IngredientLot> activeLots = lotRepository.findByStatus(LotStatus.ACTIVE);

        for (IngredientLot lot : activeLots) {
            if (lot.getExpiryDate() == null) {
                continue;
            }

            FreshnessStatus newStatus = computeStatus(lot.getExpiryDate(), now);
            ExpiryRecord record = expiryRecordRepository.findByLotId(lot.getId())
                    .orElseGet(() -> {
                        ExpiryRecord r = new ExpiryRecord();
                        r.setLot(lot);
                        r.setExpiryDate(lot.getExpiryDate());
                        return r;
                    });

            FreshnessStatus previousStatus = record.getFreshnessStatus();
            record.setFreshnessStatus(newStatus);

            if (newStatus != FreshnessStatus.FRESH && newStatus != previousStatus) {
                record.setNotifiedAt(now);
            }

            expiryRecordRepository.save(record);
        }
    }

    private FreshnessStatus computeStatus(Instant expiryDate, Instant now) {
        if (expiryDate.isBefore(now)) {
            return FreshnessStatus.EXPIRED;
        }
        long daysUntilExpiry = ChronoUnit.DAYS.between(now, expiryDate);
        if (daysUntilExpiry <= NEAR_EXPIRY_DAYS) {
            return FreshnessStatus.NEAR_EXPIRY;
        }
        return FreshnessStatus.FRESH;
    }
}
