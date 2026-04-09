package com.nourishos.authority.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.HouseholdSettings;

public interface HouseholdSettingsRepository extends JpaRepository<HouseholdSettings, UUID> {
}
