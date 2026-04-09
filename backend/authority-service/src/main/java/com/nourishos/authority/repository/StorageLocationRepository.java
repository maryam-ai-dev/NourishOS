package com.nourishos.authority.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.StorageLocation;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, UUID> {

    List<StorageLocation> findByHouseholdId(UUID householdId);
}
