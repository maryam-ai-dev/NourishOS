package com.nourishos.authority.service.audit;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.AuditRecord;
import com.nourishos.authority.domain.ExecutionStep;
import com.nourishos.authority.repository.AuditRecordRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExecutionTrace {

    private final AuditRecordRepository auditRecordRepository;
    private final ObjectMapper objectMapper;

    public AuditRecord write(ExecutionStep step, String status) {
        AuditRecord record = new AuditRecord();
        record.setEventType("EXECUTION_STEP");
        record.setEntityId(step.getId());
        record.setEntityType("ExecutionStep");
        record.setActorType("SYSTEM");
        record.setPayload(toJson(new Payload(
                step.getId().toString(), step.getActionType(), step.getAssignedTo(), status)));
        return auditRecordRepository.save(record);
    }

    private String toJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException e) { return "{}"; }
    }

    record Payload(String stepId, String actionType, String assignedTo, String status) {}
}
