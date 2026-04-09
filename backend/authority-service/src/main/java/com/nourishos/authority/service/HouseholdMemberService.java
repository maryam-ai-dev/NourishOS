package com.nourishos.authority.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.nourishos.authority.domain.Household;
import com.nourishos.authority.domain.HouseholdMember;
import com.nourishos.authority.domain.MemberPreferenceProfile;
import com.nourishos.authority.dto.CreateMemberRequest;
import com.nourishos.authority.dto.UpdateMemberRequest;
import com.nourishos.authority.repository.HouseholdMemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HouseholdMemberService {

    private final HouseholdMemberRepository memberRepository;
    private final HouseholdService householdService;

    @Transactional
    public HouseholdMember create(UUID householdId, CreateMemberRequest request) {
        Household household = householdService.findById(householdId);
        HouseholdMember member = new HouseholdMember();
        member.setHousehold(household);
        member.setDisplayName(request.getDisplayName());
        member.setAgeGroup(request.getAgeGroup());
        member.setEffortSensitivity(request.getEffortSensitivity());
        member.setParticipatesInMealPlanning(request.isParticipatesInMealPlanning());

        MemberPreferenceProfile profile = new MemberPreferenceProfile();
        profile.setMember(member);
        member.setPreferenceProfile(profile);

        return memberRepository.save(member);
    }

    public List<HouseholdMember> findByHouseholdId(UUID householdId) {
        householdService.findById(householdId);
        return memberRepository.findByHouseholdId(householdId);
    }

    public HouseholdMember update(UUID householdId, UUID memberId, UpdateMemberRequest request) {
        householdService.findById(householdId);
        HouseholdMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        member.setDisplayName(request.getDisplayName());
        member.setAgeGroup(request.getAgeGroup());
        member.setEffortSensitivity(request.getEffortSensitivity());
        member.setParticipatesInMealPlanning(request.isParticipatesInMealPlanning());
        return memberRepository.save(member);
    }
}
