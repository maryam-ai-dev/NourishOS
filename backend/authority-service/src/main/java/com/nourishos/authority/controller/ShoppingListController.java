package com.nourishos.authority.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nourishos.authority.domain.ReplenishmentSuggestion;
import com.nourishos.authority.domain.ShoppingListItem;
import com.nourishos.authority.repository.ReplenishmentSuggestionRepository;
import com.nourishos.authority.repository.ShoppingListItemRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/replenishment/requests/{requestId}/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListItemRepository itemRepository;
    private final ReplenishmentSuggestionRepository suggestionRepository;

    @GetMapping
    public List<ShoppingListItem> list(@PathVariable UUID requestId) {
        return itemRepository.findByReplenishmentRequestId(requestId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public List<ShoppingListItem> generate(@PathVariable UUID requestId) {
        // Generate shopping list from approved suggestions for this request
        List<ReplenishmentSuggestion> approved = suggestionRepository.findAll().stream()
                .filter(s -> "APPROVED".equals(s.getStatus()))
                .toList();

        List<ShoppingListItem> items = approved.stream().map(sug -> {
            ShoppingListItem item = new ShoppingListItem();
            item.setReplenishmentRequestId(requestId);
            item.setIngredientId(sug.getIngredient() != null ? sug.getIngredient().getId() : null);
            item.setIngredientName(sug.getIngredient() != null ? sug.getIngredient().getName() : "Unknown");
            item.setQuantity(sug.getSuggestedQuantity() != null ? sug.getSuggestedQuantity() : BigDecimal.ONE);
            item.setUnit(sug.getUnit() != null ? sug.getUnit() : "unit");
            item.setCategory("OTHER");
            return itemRepository.save(item);
        }).toList();

        return items;
    }

    @PatchMapping("/{itemId}/check")
    @Transactional
    public ShoppingListItem check(@PathVariable UUID requestId, @PathVariable UUID itemId) {
        ShoppingListItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (item.isChecked()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item already checked");
        }
        item.setChecked(true);
        item.setCheckedAt(Instant.now());
        return itemRepository.save(item);
    }
}
