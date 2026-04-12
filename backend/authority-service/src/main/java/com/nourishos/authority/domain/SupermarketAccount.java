package com.nourishos.authority.domain;

import java.time.Instant;
import java.util.UUID;

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
@Table(name = "supermarket_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupermarketAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "household_id", nullable = false)
    private UUID householdId;

    @Column(name = "supermarket_name", nullable = false, length = 30)
    private String supermarketName; // TESCO / SAINSBURYS / OCADO / ASDA / WAITROSE

    @Column(name = "account_email", length = 200)
    private String accountEmail;

    @Column(name = "is_connected", nullable = false)
    private boolean isConnected = true;

    @Column(name = "connected_at", nullable = false)
    private Instant connectedAt;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @jakarta.persistence.PrePersist
    void prePersist() {
        if (connectedAt == null) connectedAt = Instant.now();
    }
}
