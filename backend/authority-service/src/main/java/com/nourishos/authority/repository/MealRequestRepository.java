package com.nourishos.authority.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.MealRequest;
import com.nourishos.authority.domain.RequestType;

public interface MealRequestRepository extends JpaRepository<MealRequest, UUID> {

    Optional<MealRequest> findFirstByHouseholdIdAndRequestTypeAndRequestedAtAfter(
            UUID householdId, RequestType requestType, Instant after);
}
