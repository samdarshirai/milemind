package com.company.runcoach.adaptation.domain;

import com.company.runcoach.identity.domain.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "fatigue_signal")
public class FatigueSignal {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "signal_date", nullable = false)
    private LocalDate signalDate;

    @Column(name = "sleep_score", nullable = false)
    private int sleepScore;

    @Column(name = "stress_score", nullable = false)
    private int stressScore;

    @Column(name = "soreness_score", nullable = false)
    private int sorenessScore;

    @Column(name = "motivation_score", nullable = false)
    private int motivationScore;

    @Column(name = "illness_flag", nullable = false)
    private boolean illnessFlag;

    @Column(name = "too_busy_flag", nullable = false)
    private boolean tooBusyFlag;

    @Column(name = "travelling_flag", nullable = false)
    private boolean travellingFlag;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public LocalDate getSignalDate() { return signalDate; }
    public void setSignalDate(LocalDate signalDate) { this.signalDate = signalDate; }
    public int getSleepScore() { return sleepScore; }
    public void setSleepScore(int sleepScore) { this.sleepScore = sleepScore; }
    public int getStressScore() { return stressScore; }
    public void setStressScore(int stressScore) { this.stressScore = stressScore; }
    public int getSorenessScore() { return sorenessScore; }
    public void setSorenessScore(int sorenessScore) { this.sorenessScore = sorenessScore; }
    public int getMotivationScore() { return motivationScore; }
    public void setMotivationScore(int motivationScore) { this.motivationScore = motivationScore; }
    public boolean isIllnessFlag() { return illnessFlag; }
    public void setIllnessFlag(boolean illnessFlag) { this.illnessFlag = illnessFlag; }
    public boolean isTooBusyFlag() { return tooBusyFlag; }
    public void setTooBusyFlag(boolean tooBusyFlag) { this.tooBusyFlag = tooBusyFlag; }
    public boolean isTravellingFlag() { return travellingFlag; }
    public void setTravellingFlag(boolean travellingFlag) { this.travellingFlag = travellingFlag; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
