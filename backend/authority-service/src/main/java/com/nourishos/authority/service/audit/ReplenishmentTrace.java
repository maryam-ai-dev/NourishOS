package com.nourishos.authority.service.audit;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.AuditRecord;
import com.nourishos.authority.domain.ReplenishmentSuggestion;
import com.nourishos.authority.repository.AuditRecordRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReplenishmentTrace {

    private final AuditRecordRepository auditRecordRepository;
    private final ObjectMapper objectMapper;

    public AuditRecord write(ReplenishmentSuggestion suggestion, String decision) {
        AuditRecord record = new AuditRecord();
        record.setEventType("REPLENISHMENT_DECISION");
        record.setEntityId(suggestion.getId());
        record.setEntityType("ReplenishmentSuggestion");
        record.setActorType("SYSTEM");
        record.setPayload(toJson(new Payload(
                suggestion.getId().toString(), decision,
                String.valueOf(suggestion.isAdjustedForWaste()),
                suggestion.getReason())));
        return auditRecordRepository.save(record);
    }

    private String toJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException e) { return "{}"; }
    }

    record Payload(String suggestionId, String decision, String adjustedForWaste, String reason) {}
}
