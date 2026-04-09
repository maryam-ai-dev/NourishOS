package com.nourishos.authority.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.dto.CreateMemberRequest;
import com.nourishos.authority.dto.MemberResponse;
import com.nourishos.authority.dto.UpdateMemberRequest;
import com.nourishos.authority.service.HouseholdMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/households/{householdId}/members")
@RequiredArgsConstructor
public class HouseholdMemberController {

    private final HouseholdMemberService memberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse create(@PathVariable UUID householdId,
                                 @Valid @RequestBody CreateMemberRequest request) {
        return MemberResponse.from(memberService.create(householdId, request));
    }

    @GetMapping
    public List<MemberResponse> list(@PathVariable UUID householdId) {
        return memberService.findByHouseholdId(householdId).stream()
                .map(MemberResponse::from)
                .toList();
    }

    @PutMapping("/{memberId}")
    public MemberResponse update(@PathVariable UUID householdId,
                                 @PathVariable UUID memberId,
                                 @Valid @RequestBody UpdateMemberRequest request) {
        return MemberResponse.from(memberService.update(householdId, memberId, request));
    }
}
