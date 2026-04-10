package com.nourishos.authority.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.MealOption;

public interface MealOptionRepository extends JpaRepository<MealOption, UUID> {
}
