package com.nourishos.authority.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.MemberPreferenceProfile;

public interface MemberPreferenceProfileRepository extends JpaRepository<MemberPreferenceProfile, UUID> {

    Optional<MemberPreferenceProfile> findByMemberId(UUID memberId);
}
