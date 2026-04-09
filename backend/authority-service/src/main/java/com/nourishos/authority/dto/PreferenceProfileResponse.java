package com.nourishos.authority.dto;

import java.util.UUID;

import com.nourishos.authority.domain.MemberPreferenceProfile;
import lombok.Data;

@Data
public class PreferenceProfileResponse {

    private UUID id;
    private UUID memberId;
    private String dislikedIngredients;
    private String dietaryRestrictions;
    private Integer proteinGoalGrams;
    private String preferredMealTypes;

    public static PreferenceProfileResponse from(MemberPreferenceProfile profile) {
        PreferenceProfileResponse r = new PreferenceProfileResponse();
        r.setId(profile.getId());
        r.setMemberId(profile.getMember().getId());
        r.setDislikedIngredients(profile.getDislikedIngredients());
        r.setDietaryRestrictions(profile.getDietaryRestrictions());
        r.setProteinGoalGrams(profile.getProteinGoalGrams());
        r.setPreferredMealTypes(profile.getPreferredMealTypes());
        return r;
    }
}
