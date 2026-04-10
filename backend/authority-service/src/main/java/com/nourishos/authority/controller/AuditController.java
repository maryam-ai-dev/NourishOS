package com.nourishos.authority.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<AuditRecord> list() {
        return auditRecordRepository.findAll();
    }

    @GetMapping("/executions/{id}")
    public List<AuditRecord> executionTrace(@PathVariable UUID id) {
        return auditRecordRepository.findByEntityIdOrderByCreatedAtDesc(id);
    }
}
