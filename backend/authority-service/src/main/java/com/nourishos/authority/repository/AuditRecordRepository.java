package com.nourishos.authority.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nourishos.authority.domain.AuditRecord;

public interface AuditRecordRepository extends JpaRepository<AuditRecord, UUID> {

    List<AuditRecord> findByEventTypeOrderByCreatedAtDesc(String eventType);

    List<AuditRecord> findByEntityIdOrderByCreatedAtDesc(UUID entityId);
}
