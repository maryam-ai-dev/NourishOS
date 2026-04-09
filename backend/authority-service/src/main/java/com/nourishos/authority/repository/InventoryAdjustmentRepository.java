package com.nourishos.authority.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.InventoryAdjustment;

public interface InventoryAdjustmentRepository extends JpaRepository<InventoryAdjustment, UUID> {

    List<InventoryAdjustment> findByLotIdOrderByAdjustedAtDesc(UUID lotId);
}
