package com.nourishos.authority.domain;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "execution_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private ExecutionPlan plan;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "assigned_to", nullable = false)
    private String assignedTo;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "estimated_duration_seconds")
    private Integer estimatedDurationSeconds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ingredient_ref", columnDefinition = "jsonb")
    private String ingredientRef;
}
