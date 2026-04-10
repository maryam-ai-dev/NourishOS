package com.nourishos.authority.domain;

import java.time.Instant;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "approval_decisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replenishment_request_id", nullable = false)
    private ReplenishmentRequest replenishmentRequest;

    @Column(name = "decided_by")
    private String decidedBy;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    @Column(nullable = false)
    private String decision;

    @Column(length = 500)
    private String notes;

    @jakarta.persistence.PrePersist
    void prePersist() {
        if (decidedAt == null) {
            decidedAt = Instant.now();
        }
    }
}
