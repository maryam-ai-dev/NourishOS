package com.nourishos.authority.domain;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "policy_rules", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"policy_set_id", "rule_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_set_id", nullable = false)
    private PolicySet policySet;

    @Column(name = "rule_type", nullable = false)
    private String ruleType;

    @Column
    private String value;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
