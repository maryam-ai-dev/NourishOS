package com.nourishos.authority.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "policy_sets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicySet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "household_id", nullable = false, unique = true)
    private Household household;

    @Column(name = "auto_reorder_limit", nullable = false)
    private BigDecimal autoReorderLimit = new BigDecimal("50.00");

    @Column(name = "substitution_approval_required", nullable = false)
    private boolean substitutionApprovalRequired = true;

    @Column(name = "night_mode_enabled", nullable = false)
    private boolean nightModeEnabled = false;

    @Column(name = "max_autonomous_actions", nullable = false)
    private int maxAutonomousActions = 5;

    @Column(name = "waste_alert_threshold", nullable = false)
    private BigDecimal wasteAlertThreshold = new BigDecimal("0.30");
}
