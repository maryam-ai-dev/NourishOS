package com.nourishos.authority.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nourishos.authority.domain.ShoppingListItem;

public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, UUID> {

    List<ShoppingListItem> findByReplenishmentRequestId(UUID requestId);
}
