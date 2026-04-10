package com.nourishos.authority.service.audit;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.domain.AuditRecord;
import com.nourishos.authority.domain.ConsumptionEvent;
import com.nourishos.authority.domain.MealOutcomeEvent;
import com.nourishos.authority.domain.WasteEvent;
import com.nourishos.authority.repository.AuditRecordRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FoodFlowTrace {

    private final AuditRecordRepository auditRecordRepository;
    private final ObjectMapper objectMapper;

    public AuditRecord write(ConsumptionEvent event) {
        return writeRecord("CONSUMPTION", event.getId(), "ConsumptionEvent",
                toJson(new ConsumptionPayload(event.getSource().name(), event.getQuantity().toString(), event.getUnit())));
    }

    public AuditRecord write(WasteEvent event) {
        return writeRecord("WASTE", event.getId(), "WasteEvent",
                toJson(new WastePayload(event.getWasteReason().name(), event.getQuantity().toString(), event.getUnit())));
    }

    public AuditRecord write(MealOutcomeEvent event) {
        return writeRecord("MEAL_OUTCOME", event.getId(), "MealOutcomeEvent",
                toJson(new OutcomePayload(event.getOutcome().name(), event.getNotes())));
    }

    private AuditRecord writeRecord(String eventType, UUID entityId, String entityType, String payload) {
        AuditRecord record = new AuditRecord();
        record.setEventType(eventType);
        record.setEntityId(entityId);
        record.setEntityType(entityType);
        record.setActorType("SYSTEM");
        record.setPayload(payload);
        return auditRecordRepository.save(record);
    }

    private String toJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException e) { return "{}"; }
    }

    record ConsumptionPayload(String source, String quantity, String unit) {}
    record WastePayload(String wasteReason, String quantity, String unit) {}
    record OutcomePayload(String outcome, String notes) {}
}
