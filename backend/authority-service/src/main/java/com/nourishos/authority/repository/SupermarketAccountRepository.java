package com.nourishos.authority.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nourishos.authority.domain.SupermarketAccount;

public interface SupermarketAccountRepository extends JpaRepository<SupermarketAccount, UUID> {

    List<SupermarketAccount> findByHouseholdId(UUID householdId);

    Optional<SupermarketAccount> findByHouseholdIdAndSupermarketName(UUID householdId, String supermarketName);
}
