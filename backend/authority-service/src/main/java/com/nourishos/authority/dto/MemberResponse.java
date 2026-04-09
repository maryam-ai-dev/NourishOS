package com.nourishos.authority.dto;

import java.util.UUID;

import com.nourishos.authority.domain.AgeGroup;
import com.nourishos.authority.domain.HouseholdMember;
import lombok.Data;

@Data
public class MemberResponse {

    private UUID id;
    private UUID householdId;
    private String displayName;
    private AgeGroup ageGroup;
    private String effortSensitivity;
    private boolean participatesInMealPlanning;

    public static MemberResponse from(HouseholdMember member) {
        MemberResponse r = new MemberResponse();
        r.setId(member.getId());
        r.setHouseholdId(member.getHousehold().getId());
        r.setDisplayName(member.getDisplayName());
        r.setAgeGroup(member.getAgeGroup());
        r.setEffortSensitivity(member.getEffortSensitivity());
        r.setParticipatesInMealPlanning(member.isParticipatesInMealPlanning());
        return r;
    }
}
