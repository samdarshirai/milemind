package com.company.runcoach.adaptation.domain;

import com.company.runcoach.identity.domain.AppUser;
import com.company.runcoach.planning.domain.TrainingPlan;
import com.company.runcoach.profile.domain.JsonObjectConverter;
import com.company.runcoach.profile.domain.JsonStringListConverter;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "adaptation_decision")
public class AdaptationDecision {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_plan_id", nullable = false)
    private TrainingPlan trainingPlan;

    @Column(name = "plan_version_before", nullable = false)
    private int planVersionBefore;

    @Column(name = "plan_version_after", nullable = false)
    private int planVersionAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private AdaptationTriggerType triggerType;

    @Column(name = "trigger_workout_id")
    private UUID triggerWorkoutId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private AdaptationReason reason;

    @Column(name = "decision_type", nullable = false)
    private String decisionType;

    @Column(name = "decision_scope", nullable = false)
    private String decisionScope;

    @Column(name = "confidence", nullable = false)
    private java.math.BigDecimal confidence;

    @Convert(converter = JsonStringListConverter.class)
    @Column(name = "reason_codes", nullable = false, columnDefinition = "jsonb")
    private List<String> reasonCodes;

    @Convert(converter = JsonObjectConverter.class)
    @Column(name = "before_state_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> beforeState;

    @Convert(converter = JsonObjectConverter.class)
    @Column(name = "after_state_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> afterState;

    @Column(name = "affected_from_date", nullable = false)
    private LocalDate affectedFromDate;

    @Column(name = "affected_to_date", nullable = false)
    private LocalDate affectedToDate;

    @Column(name = "decision_summary", nullable = false)
    private String decisionSummary;

    @Convert(converter = JsonStringListConverter.class)
    @Column(name = "changed_workout_ids", nullable = false, columnDefinition = "jsonb")
    private List<String> changedWorkoutIds;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public TrainingPlan getTrainingPlan() { return trainingPlan; }
    public void setTrainingPlan(TrainingPlan trainingPlan) { this.trainingPlan = trainingPlan; }
    public int getPlanVersionBefore() { return planVersionBefore; }
    public void setPlanVersionBefore(int planVersionBefore) { this.planVersionBefore = planVersionBefore; }
    public int getPlanVersionAfter() { return planVersionAfter; }
    public void setPlanVersionAfter(int planVersionAfter) { this.planVersionAfter = planVersionAfter; }
    public AdaptationTriggerType getTriggerType() { return triggerType; }
    public void setTriggerType(AdaptationTriggerType triggerType) { this.triggerType = triggerType; }
    public UUID getTriggerWorkoutId() { return triggerWorkoutId; }
    public void setTriggerWorkoutId(UUID triggerWorkoutId) { this.triggerWorkoutId = triggerWorkoutId; }
    public AdaptationReason getReason() { return reason; }
    public void setReason(AdaptationReason reason) { this.reason = reason; }
    public String getDecisionType() { return decisionType; }
    public void setDecisionType(String decisionType) { this.decisionType = decisionType; }
    public String getDecisionScope() { return decisionScope; }
    public void setDecisionScope(String decisionScope) { this.decisionScope = decisionScope; }
    public java.math.BigDecimal getConfidence() { return confidence; }
    public void setConfidence(java.math.BigDecimal confidence) { this.confidence = confidence; }
    public List<String> getReasonCodes() { return reasonCodes; }
    public void setReasonCodes(List<String> reasonCodes) { this.reasonCodes = reasonCodes; }
    public Map<String, Object> getBeforeState() { return beforeState; }
    public void setBeforeState(Map<String, Object> beforeState) { this.beforeState = beforeState; }
    public Map<String, Object> getAfterState() { return afterState; }
    public void setAfterState(Map<String, Object> afterState) { this.afterState = afterState; }
    public LocalDate getAffectedFromDate() { return affectedFromDate; }
    public void setAffectedFromDate(LocalDate affectedFromDate) { this.affectedFromDate = affectedFromDate; }
    public LocalDate getAffectedToDate() { return affectedToDate; }
    public void setAffectedToDate(LocalDate affectedToDate) { this.affectedToDate = affectedToDate; }
    public String getDecisionSummary() { return decisionSummary; }
    public void setDecisionSummary(String decisionSummary) { this.decisionSummary = decisionSummary; }
    public List<String> getChangedWorkoutIds() { return changedWorkoutIds; }
    public void setChangedWorkoutIds(List<String> changedWorkoutIds) { this.changedWorkoutIds = changedWorkoutIds; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
