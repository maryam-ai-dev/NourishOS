package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LotAllocation {

    private UUID lotId;
    private BigDecimal quantityFromThisLot;
    private String unit;
}
