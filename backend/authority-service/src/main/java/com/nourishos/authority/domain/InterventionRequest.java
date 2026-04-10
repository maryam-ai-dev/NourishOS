package com.nourishos.authority.domain;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "intervention_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InterventionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private ExecutionPlan plan;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private ExecutionStep step;

    @Enumerated(EnumType.STRING)
    @Column(name = "intervention_type", nullable = false)
    private InterventionType interventionType;

    @Column(length = 500)
    private String message;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public void resolve() {
        if ("RESOLVED".equals(this.status)) {
            throw new IllegalStateException("Intervention already resolved: " + this.id);
        }
        this.status = "RESOLVED";
        this.resolvedAt = Instant.now();
    }
}
