package com.nourishos.authority.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.IngredientLot;
import com.nourishos.authority.domain.LotStatus;

public interface IngredientLotRepository extends JpaRepository<IngredientLot, UUID> {

    List<IngredientLot> findByIngredientIdAndStatus(UUID ingredientId, LotStatus status);

    List<IngredientLot> findByStorageLocationIdAndStatus(UUID storageLocationId, LotStatus status);

    List<IngredientLot> findByStatus(LotStatus status);
}
