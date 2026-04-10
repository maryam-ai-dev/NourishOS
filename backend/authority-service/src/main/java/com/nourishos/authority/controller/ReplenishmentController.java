package com.nourishos.authority.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.dto.SuggestionResponse;
import com.nourishos.authority.repository.ReplenishmentSuggestionRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/replenishment")
@RequiredArgsConstructor
public class ReplenishmentController {

    private static final Map<String, Integer> URGENCY_ORDER = Map.of(
            "CRITICAL", 0, "HIGH", 1, "MEDIUM", 2, "LOW", 3);

    private final ReplenishmentSuggestionRepository suggestionRepository;

    @GetMapping("/suggestions")
    @Transactional(readOnly = true)
    public List<SuggestionResponse> suggestions(@RequestParam UUID householdId) {
        return suggestionRepository.findByHouseholdId(householdId).stream()
                .map(SuggestionResponse::from)
                .sorted(Comparator.comparingInt(s -> URGENCY_ORDER.getOrDefault(s.getUrgency(), 99)))
                .toList();
    }
}
