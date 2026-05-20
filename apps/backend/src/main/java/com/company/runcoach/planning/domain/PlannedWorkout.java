package com.company.runcoach.planning.domain;

import com.company.runcoach.identity.domain.AppUser;
import com.company.runcoach.profile.domain.JsonObjectConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "planned_workout")
public class PlannedWorkout {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_plan_id", nullable = false)
    private TrainingPlan trainingPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_week_id", nullable = false)
    private TrainingPlanWeek trainingPlanWeek;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "workout_type", nullable = false)
    private PlannedWorkoutType workoutType;

    @Column(name = "workout_subtype")
    private String workoutSubtype;

    @Column(name = "planned_distance_km")
    private BigDecimal plannedDistanceKm;

    @Column(name = "planned_duration_min")
    private Integer plannedDurationMin;

    @Column(name = "intensity_zone")
    private String intensityZone;

    @Convert(converter = JsonObjectConverter.class)
    @Column(name = "structure_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> structure;

    @Convert(converter = JsonObjectConverter.class)
    @Column(name = "rationale_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> rationale;

    @Column(name = "adapted_from_workout_id")
    private UUID adaptedFromWorkoutId;

    @Column(name = "plan_version", nullable = false)
    private int planVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PlannedWorkoutStatus status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public TrainingPlan getTrainingPlan() { return trainingPlan; }
    public void setTrainingPlan(TrainingPlan trainingPlan) { this.trainingPlan = trainingPlan; }
    public TrainingPlanWeek getTrainingPlanWeek() { return trainingPlanWeek; }
    public void setTrainingPlanWeek(TrainingPlanWeek trainingPlanWeek) { this.trainingPlanWeek = trainingPlanWeek; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    public PlannedWorkoutType getWorkoutType() { return workoutType; }
    public void setWorkoutType(PlannedWorkoutType workoutType) { this.workoutType = workoutType; }
    public String getWorkoutSubtype() { return workoutSubtype; }
    public void setWorkoutSubtype(String workoutSubtype) { this.workoutSubtype = workoutSubtype; }
    public BigDecimal getPlannedDistanceKm() { return plannedDistanceKm; }
    public void setPlannedDistanceKm(BigDecimal plannedDistanceKm) { this.plannedDistanceKm = plannedDistanceKm; }
    public Integer getPlannedDurationMin() { return plannedDurationMin; }
    public void setPlannedDurationMin(Integer plannedDurationMin) { this.plannedDurationMin = plannedDurationMin; }
    public String getIntensityZone() { return intensityZone; }
    public void setIntensityZone(String intensityZone) { this.intensityZone = intensityZone; }
    public Map<String, Object> getStructure() { return structure; }
    public void setStructure(Map<String, Object> structure) { this.structure = structure; }
    public Map<String, Object> getRationale() { return rationale; }
    public void setRationale(Map<String, Object> rationale) { this.rationale = rationale; }
    public UUID getAdaptedFromWorkoutId() { return adaptedFromWorkoutId; }
    public void setAdaptedFromWorkoutId(UUID adaptedFromWorkoutId) { this.adaptedFromWorkoutId = adaptedFromWorkoutId; }
    public int getPlanVersion() { return planVersion; }
    public void setPlanVersion(int planVersion) { this.planVersion = planVersion; }
    public PlannedWorkoutStatus getStatus() { return status; }
    public void setStatus(PlannedWorkoutStatus status) { this.status = status; }
}
