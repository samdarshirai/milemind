package com.company.runcoach.adaptation.domain;

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
@Table(name = "injury_feedback")
public class InjuryFeedback {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "reported_at", nullable = false)
    private OffsetDateTime reportedAt;

    @Column(name = "has_pain", nullable = false)
    private boolean hasPain;

    @Column(name = "body_region")
    private String bodyRegion;

    @Column(name = "pain_type")
    private String painType;

    @Column(name = "severity")
    private Integer severity;

    @Column(name = "onset_context")
    private String onsetContext;

    @Column(name = "can_run")
    private Boolean canRun;

    @Column(name = "red_flag", nullable = false)
    private boolean redFlag;

    @Column(name = "free_text")
    private String freeText;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public OffsetDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(OffsetDateTime reportedAt) { this.reportedAt = reportedAt; }
    public boolean isHasPain() { return hasPain; }
    public void setHasPain(boolean hasPain) { this.hasPain = hasPain; }
    public String getBodyRegion() { return bodyRegion; }
    public void setBodyRegion(String bodyRegion) { this.bodyRegion = bodyRegion; }
    public String getPainType() { return painType; }
    public void setPainType(String painType) { this.painType = painType; }
    public Integer getSeverity() { return severity; }
    public void setSeverity(Integer severity) { this.severity = severity; }
    public String getOnsetContext() { return onsetContext; }
    public void setOnsetContext(String onsetContext) { this.onsetContext = onsetContext; }
    public Boolean getCanRun() { return canRun; }
    public void setCanRun(Boolean canRun) { this.canRun = canRun; }
    public boolean isRedFlag() { return redFlag; }
    public void setRedFlag(boolean redFlag) { this.redFlag = redFlag; }
    public String getFreeText() { return freeText; }
    public void setFreeText(String freeText) { this.freeText = freeText; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
