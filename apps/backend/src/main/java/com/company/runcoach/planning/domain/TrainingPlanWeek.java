package com.company.runcoach.planning.domain;

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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "training_plan_week")
public class TrainingPlanWeek {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_plan_id", nullable = false)
    private TrainingPlan trainingPlan;

    @Column(name = "week_index", nullable = false)
    private int weekIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase", nullable = false)
    private TrainingPhase phase;

    @Column(name = "target_distance_km")
    private BigDecimal targetDistanceKm;

    @Column(name = "target_time_min")
    private Integer targetTimeMin;

    @Column(name = "recovery_week", nullable = false)
    private boolean recoveryWeek;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public TrainingPlan getTrainingPlan() { return trainingPlan; }
    public void setTrainingPlan(TrainingPlan trainingPlan) { this.trainingPlan = trainingPlan; }
    public int getWeekIndex() { return weekIndex; }
    public void setWeekIndex(int weekIndex) { this.weekIndex = weekIndex; }
    public TrainingPhase getPhase() { return phase; }
    public void setPhase(TrainingPhase phase) { this.phase = phase; }
    public BigDecimal getTargetDistanceKm() { return targetDistanceKm; }
    public void setTargetDistanceKm(BigDecimal targetDistanceKm) { this.targetDistanceKm = targetDistanceKm; }
    public Integer getTargetTimeMin() { return targetTimeMin; }
    public void setTargetTimeMin(Integer targetTimeMin) { this.targetTimeMin = targetTimeMin; }
    public boolean isRecoveryWeek() { return recoveryWeek; }
    public void setRecoveryWeek(boolean recoveryWeek) { this.recoveryWeek = recoveryWeek; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
