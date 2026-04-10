package com.nourishos.authority.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.HouseholdInsight;
import com.nourishos.authority.repository.HouseholdInsightRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/insights")
@RequiredArgsConstructor
public class InsightController {

    private final HouseholdInsightRepository insightRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<HouseholdInsight> upsertInsights(@RequestBody List<InsightRequest> requests) {
        return requests.stream().map(this::upsert).toList();
    }

    @GetMapping("/{householdId}")
    public List<HouseholdInsight> listInsights(@PathVariable UUID householdId) {
        return insightRepository.findByHouseholdIdOrderByCreatedAtDesc(householdId);
    }

    private HouseholdInsight upsert(InsightRequest request) {
        return insightRepository
                .findByHouseholdIdAndSnapshotWeekAndCategory(
                        request.householdId(), request.snapshotWeek(), request.category())
                .map(existing -> {
                    existing.setInsightText(request.insightText());
                    return insightRepository.save(existing);
                })
                .orElseGet(() -> {
                    HouseholdInsight insight = new HouseholdInsight();
                    insight.setHouseholdId(request.householdId());
                    insight.setSnapshotWeek(request.snapshotWeek());
                    insight.setCategory(request.category());
                    insight.setInsightText(request.insightText());
                    return insightRepository.save(insight);
                });
    }

    public record InsightRequest(UUID householdId, LocalDate snapshotWeek, String category, String insightText) {}
}
