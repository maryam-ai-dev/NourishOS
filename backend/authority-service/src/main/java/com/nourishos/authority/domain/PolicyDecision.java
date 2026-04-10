package com.nourishos.authority.domain;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "policy_decisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_type", nullable = false)
    private String ruleType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String input = "{}";

    @Column(nullable = false)
    private String decision;

    @Column(length = 500)
    private String reason;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    @jakarta.persistence.PrePersist
    void prePersist() {
        if (decidedAt == null) {
            decidedAt = Instant.now();
        }
    }
}
