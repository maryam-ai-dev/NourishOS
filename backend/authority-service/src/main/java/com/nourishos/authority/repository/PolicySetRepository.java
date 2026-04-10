package com.nourishos.authority.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.PolicySet;

public interface PolicySetRepository extends JpaRepository<PolicySet, UUID> {

    Optional<PolicySet> findByHouseholdId(UUID householdId);
}
