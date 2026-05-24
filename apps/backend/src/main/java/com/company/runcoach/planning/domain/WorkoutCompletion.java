package com.company.runcoach.planning.domain;

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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "workout_completion")
public class WorkoutCompletion {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planned_workout_id", nullable = false)
    private PlannedWorkout plannedWorkout;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "completion_source", nullable = false)
    private WorkoutCompletionSource completionSource;

    @Column(name = "completed_at", nullable = false)
    private OffsetDateTime completedAt;

    @Column(name = "actual_distance_km")
    private BigDecimal actualDistanceKm;

    @Column(name = "actual_duration_min")
    private Integer actualDurationMin;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public PlannedWorkout getPlannedWorkout() { return plannedWorkout; }
    public void setPlannedWorkout(PlannedWorkout plannedWorkout) { this.plannedWorkout = plannedWorkout; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public WorkoutCompletionSource getCompletionSource() { return completionSource; }
    public void setCompletionSource(WorkoutCompletionSource completionSource) { this.completionSource = completionSource; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
    public BigDecimal getActualDistanceKm() { return actualDistanceKm; }
    public void setActualDistanceKm(BigDecimal actualDistanceKm) { this.actualDistanceKm = actualDistanceKm; }
    public Integer getActualDurationMin() { return actualDurationMin; }
    public void setActualDurationMin(Integer actualDurationMin) { this.actualDurationMin = actualDurationMin; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
