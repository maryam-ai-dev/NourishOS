package com.nourishos.authority.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietaryRule {

    private DietaryRuleType ruleType;
    private String value;
}
