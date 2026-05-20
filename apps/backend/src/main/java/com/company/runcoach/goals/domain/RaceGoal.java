package com.company.runcoach.goals.domain;

import com.company.runcoach.identity.domain.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "race_goal")
public class RaceGoal {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "race_name")
    private String raceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "race_distance_type", nullable = false)
    private RaceDistanceType raceDistanceType;

    @Column(name = "race_date", nullable = false)
    private LocalDate raceDate;

    @Column(name = "target_time_seconds")
    private Integer targetTimeSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_style", nullable = false)
    private GoalStyle goalStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RaceGoalStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public String getRaceName() { return raceName; }
    public void setRaceName(String raceName) { this.raceName = raceName; }
    public RaceDistanceType getRaceDistanceType() { return raceDistanceType; }
    public void setRaceDistanceType(RaceDistanceType raceDistanceType) { this.raceDistanceType = raceDistanceType; }
    public LocalDate getRaceDate() { return raceDate; }
    public void setRaceDate(LocalDate raceDate) { this.raceDate = raceDate; }
    public Integer getTargetTimeSeconds() { return targetTimeSeconds; }
    public void setTargetTimeSeconds(Integer targetTimeSeconds) { this.targetTimeSeconds = targetTimeSeconds; }
    public GoalStyle getGoalStyle() { return goalStyle; }
    public void setGoalStyle(GoalStyle goalStyle) { this.goalStyle = goalStyle; }
    public RaceGoalStatus getStatus() { return status; }
    public void setStatus(RaceGoalStatus status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
