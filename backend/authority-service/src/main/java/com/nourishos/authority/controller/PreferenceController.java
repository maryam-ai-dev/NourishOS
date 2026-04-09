package com.nourishos.authority.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.dto.PreferenceProfileResponse;
import com.nourishos.authority.dto.UpdatePreferencesRequest;
import com.nourishos.authority.service.PreferenceProfileService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/households/{householdId}/members/{memberId}/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceProfileService preferenceService;

    @PatchMapping
    public PreferenceProfileResponse updatePreferences(@PathVariable UUID householdId,
                                                        @PathVariable UUID memberId,
                                                        @RequestBody UpdatePreferencesRequest request) {
        return PreferenceProfileResponse.from(
                preferenceService.updatePreferences(householdId, memberId, request));
    }
}
