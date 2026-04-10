package com.nourishos.authority.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.AuditRecord;
import com.nourishos.authority.repository.AuditRecordRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditRecordRepository auditRecordRepository;

    @GetMapping
    public List<AuditRecord> list(@RequestParam(required = false) String eventType,
                                   @RequestParam(required = false) UUID entityId) {
        if (eventType != null) {
            return auditRecordRepository.findByEventTypeOrderByCreatedAtDesc(eventType);
        }
        if (entityId != null) {
            return auditRecordRepository.findByEntityIdOrderByCreatedAtDesc(entityId);
        }
        return auditRecordRepository.findAll();
    }

    @GetMapping("/executions/{id}")
    public List<AuditRecord> executionTrace(@PathVariable UUID id) {
        return auditRecordRepository.findByEntityIdOrderByCreatedAtDesc(id);
    }

    @GetMapping("/foodflow/{householdId}")
    public List<AuditRecord> foodFlowHistory(@PathVariable UUID householdId) {
        return auditRecordRepository.findAll().stream()
                .filter(r -> "CONSUMPTION".equals(r.getEventType())
                        || "WASTE".equals(r.getEventType())
                        || "MEAL_OUTCOME".equals(r.getEventType()))
                .sorted(java.util.Comparator.comparing(AuditRecord::getCreatedAt))
                .toList();
    }
}
