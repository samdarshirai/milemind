package com.company.runcoach.planning.domain;

import com.company.runcoach.goals.domain.RaceGoal;
import com.company.runcoach.identity.domain.AppUser;
import com.company.runcoach.profile.domain.RunnerProfile;
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
@Table(name = "training_plan")
public class TrainingPlan {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "runner_profile_id", nullable = false)
    private RunnerProfile runnerProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "race_goal_id", nullable = false)
    private RaceGoal raceGoal;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_status", nullable = false)
    private PlanStatus status;

    @Column(name = "plan_version", nullable = false)
    private int planVersion;

    @Column(name = "methodology_code", nullable = false)
    private String methodologyCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "current_week_index", nullable = false)
    private int currentWeekIndex;

    @Column(name = "last_regenerated_at")
    private OffsetDateTime lastRegeneratedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public RunnerProfile getRunnerProfile() { return runnerProfile; }
    public void setRunnerProfile(RunnerProfile runnerProfile) { this.runnerProfile = runnerProfile; }
    public RaceGoal getRaceGoal() { return raceGoal; }
    public void setRaceGoal(RaceGoal raceGoal) { this.raceGoal = raceGoal; }
    public PlanStatus getStatus() { return status; }
    public void setStatus(PlanStatus status) { this.status = status; }
    public int getPlanVersion() { return planVersion; }
    public void setPlanVersion(int planVersion) { this.planVersion = planVersion; }
    public String getMethodologyCode() { return methodologyCode; }
    public void setMethodologyCode(String methodologyCode) { this.methodologyCode = methodologyCode; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public int getCurrentWeekIndex() { return currentWeekIndex; }
    public void setCurrentWeekIndex(int currentWeekIndex) { this.currentWeekIndex = currentWeekIndex; }
    public OffsetDateTime getLastRegeneratedAt() { return lastRegeneratedAt; }
    public void setLastRegeneratedAt(OffsetDateTime lastRegeneratedAt) { this.lastRegeneratedAt = lastRegeneratedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
