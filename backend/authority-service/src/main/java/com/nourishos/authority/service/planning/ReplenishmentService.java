package com.nourishos.authority.service.planning;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nourishos.authority.domain.Household;
import com.nourishos.authority.domain.ReplenishmentRequest;
import com.nourishos.authority.domain.ReplenishmentSuggestion;
import com.nourishos.authority.dto.BundleRequestDto;
import com.nourishos.authority.repository.ReplenishmentRequestRepository;
import com.nourishos.authority.repository.ReplenishmentSuggestionRepository;
import com.nourishos.authority.service.HouseholdService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReplenishmentService {

    private final ReplenishmentRequestRepository requestRepository;
    private final ReplenishmentSuggestionRepository suggestionRepository;
    private final HouseholdService householdService;

    @Transactional
    public ReplenishmentRequest bundleRequest(BundleRequestDto dto) {
        Household household = householdService.findById(dto.getHouseholdId());

        List<ReplenishmentSuggestion> suggestions = dto.getSuggestionIds().stream()
                .map(id -> suggestionRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Suggestion not found: " + id)))
                .toList();

        ReplenishmentRequest request = new ReplenishmentRequest();
        request.setHousehold(household);
        ReplenishmentRequest saved = requestRepository.save(request);

        for (ReplenishmentSuggestion s : suggestions) {
            s.setReplenishmentRequest(saved);
            suggestionRepository.save(s);
        }

        return saved;
    }
}
