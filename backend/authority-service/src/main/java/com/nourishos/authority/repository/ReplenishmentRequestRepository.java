package com.nourishos.authority.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.ReplenishmentRequest;

public interface ReplenishmentRequestRepository extends JpaRepository<ReplenishmentRequest, UUID> {

    List<ReplenishmentRequest> findByHouseholdId(UUID householdId);
}
