package com.nourishos.authority.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.Household;

public interface HouseholdRepository extends JpaRepository<Household, UUID> {
}
