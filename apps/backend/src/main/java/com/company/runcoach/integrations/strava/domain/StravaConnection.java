package com.company.runcoach.integrations.strava.domain;

import com.company.runcoach.identity.domain.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "strava_connection")
public class StravaConnection {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "strava_athlete_id", nullable = false)
    private long stravaAthleteId;

    @Column(name = "athlete_username")
    private String athleteUsername;

    @Column(name = "athlete_first_name")
    private String athleteFirstName;

    @Column(name = "athlete_last_name")
    private String athleteLastName;

    @Column(name = "access_token_encrypted", nullable = false)
    private String accessTokenEncrypted;

    @Column(name = "refresh_token_encrypted", nullable = false)
    private String refreshTokenEncrypted;

    @Column(name = "token_expires_at", nullable = false)
    private OffsetDateTime tokenExpiresAt;

    @Column(name = "connection_status", nullable = false)
    private String connectionStatus;

    @Column(name = "last_sync_at")
    private OffsetDateTime lastSyncAt;

    @Column(name = "scopes", nullable = false)
    private String scopes;

    @Column(name = "connected_at", nullable = false)
    private OffsetDateTime connectedAt;

    @Column(name = "disconnected_at")
    private OffsetDateTime disconnectedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public boolean isActive() {
        return disconnectedAt == null;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public long getStravaAthleteId() { return stravaAthleteId; }
    public void setStravaAthleteId(long stravaAthleteId) { this.stravaAthleteId = stravaAthleteId; }
    public String getAthleteUsername() { return athleteUsername; }
    public void setAthleteUsername(String athleteUsername) { this.athleteUsername = athleteUsername; }
    public String getAthleteFirstName() { return athleteFirstName; }
    public void setAthleteFirstName(String athleteFirstName) { this.athleteFirstName = athleteFirstName; }
    public String getAthleteLastName() { return athleteLastName; }
    public void setAthleteLastName(String athleteLastName) { this.athleteLastName = athleteLastName; }
    public String getAccessTokenEncrypted() { return accessTokenEncrypted; }
    public void setAccessTokenEncrypted(String accessTokenEncrypted) { this.accessTokenEncrypted = accessTokenEncrypted; }
    public String getRefreshTokenEncrypted() { return refreshTokenEncrypted; }
    public void setRefreshTokenEncrypted(String refreshTokenEncrypted) { this.refreshTokenEncrypted = refreshTokenEncrypted; }
    public OffsetDateTime getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(OffsetDateTime tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }
    public String getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(String connectionStatus) { this.connectionStatus = connectionStatus; }
    public OffsetDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(OffsetDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }
    public String getScopes() { return scopes; }
    public void setScopes(String scopes) { this.scopes = scopes; }
    public OffsetDateTime getConnectedAt() { return connectedAt; }
    public void setConnectedAt(OffsetDateTime connectedAt) { this.connectedAt = connectedAt; }
    public OffsetDateTime getDisconnectedAt() { return disconnectedAt; }
    public void setDisconnectedAt(OffsetDateTime disconnectedAt) { this.disconnectedAt = disconnectedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
