package com.nourishos.authority.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.InterventionRequest;

public interface InterventionRequestRepository extends JpaRepository<InterventionRequest, UUID> {

    List<InterventionRequest> findByPlanId(UUID planId);

    Optional<InterventionRequest> findByStepIdAndStatus(UUID stepId, String status);
}
