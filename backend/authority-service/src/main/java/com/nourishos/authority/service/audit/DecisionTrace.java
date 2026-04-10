package com.nourishos.authority.service.audit;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.AuditRecord;
import com.nourishos.authority.domain.PolicyDecision;
import com.nourishos.authority.repository.AuditRecordRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DecisionTrace {

    private final AuditRecordRepository auditRecordRepository;
    private final ObjectMapper objectMapper;

    public AuditRecord write(PolicyDecision decision) {
        AuditRecord record = new AuditRecord();
        record.setEventType("POLICY_DECISION");
        record.setEntityId(decision.getId());
        record.setEntityType("PolicyDecision");
        record.setActorType("SYSTEM");
        record.setPayload(toJson(new Payload(
                decision.getRuleType(), decision.getDecision(), decision.getReason())));
        return auditRecordRepository.save(record);
    }

    private String toJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException e) { return "{}"; }
    }

    record Payload(String ruleType, String decision, String reason) {}
}
