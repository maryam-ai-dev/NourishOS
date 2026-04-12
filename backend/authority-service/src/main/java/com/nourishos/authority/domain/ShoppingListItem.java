package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shopping_list_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "replenishment_request_id", nullable = false)
    private UUID replenishmentRequestId;

    @Column(name = "ingredient_id")
    private UUID ingredientId;

    @Column(name = "ingredient_name", nullable = false, length = 200)
    private String ingredientName;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false, length = 20)
    private String category = "OTHER";

    @Column(name = "is_checked", nullable = false)
    private boolean isChecked = false;

    @Column(name = "checked_at")
    private Instant checkedAt;
}
