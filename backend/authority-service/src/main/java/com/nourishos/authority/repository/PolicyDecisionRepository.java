package com.nourishos.authority.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.PolicyDecision;

public interface PolicyDecisionRepository extends JpaRepository<PolicyDecision, UUID> {

    List<PolicyDecision> findByRuleTypeOrderByDecidedAtDesc(String ruleType);
}
