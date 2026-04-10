package com.nourishos.authority.dto;

import java.time.Instant;
import java.util.UUID;

import com.nourishos.authority.domain.MealRequest;
import com.nourishos.authority.domain.MealRequestStatus;
import com.nourishos.authority.domain.RequestType;
import lombok.Data;

@Data
public class MealRequestResponse {

    private UUID id;
    private UUID householdId;
    private RequestType requestType;
    private Instant requestedAt;
    private MealRequestStatus status;

    public static MealRequestResponse from(MealRequest request) {
        MealRequestResponse r = new MealRequestResponse();
        r.setId(request.getId());
        r.setHouseholdId(request.getHousehold().getId());
        r.setRequestType(request.getRequestType());
        r.setRequestedAt(request.getRequestedAt());
        r.setStatus(request.getStatus());
        return r;
    }
}
