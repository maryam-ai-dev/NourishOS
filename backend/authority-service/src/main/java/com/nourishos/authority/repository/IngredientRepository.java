package com.nourishos.authority.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {

    Optional<Ingredient> findByName(String name);

    boolean existsByName(String name);
}
