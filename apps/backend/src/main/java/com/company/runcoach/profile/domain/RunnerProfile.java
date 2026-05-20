package com.company.runcoach.profile.domain;

import com.company.runcoach.identity.domain.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "runner_profile")
public class RunnerProfile {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(name = "birth_year", nullable = false)
    private int birthYear;

    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", nullable = false)
    private ExperienceLevel experienceLevel;

    @Column(name = "typical_weekly_distance_km", nullable = false)
    private BigDecimal typicalWeeklyDistanceKm;

    @Column(name = "longest_recent_run_km", nullable = false)
    private BigDecimal longestRecentRunKm;

    @Convert(converter = JsonStringListConverter.class)
    @Column(name = "preferred_run_days", nullable = false, columnDefinition = "jsonb")
    private List<String> preferredRunDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_long_run_day", nullable = false)
    private Weekday preferredLongRunDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_style", nullable = false)
    private GoalStyle goalStyle;

    @Convert(converter = JsonObjectConverter.class)
    @Column(name = "injury_history", columnDefinition = "jsonb")
    private Map<String, Object> injuryHistory;

    @Column(name = "strength_days_per_week", nullable = false)
    private int strengthDaysPerWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "units", nullable = false)
    private Units units;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public int getBirthYear() { return birthYear; }
    public void setBirthYear(int birthYear) { this.birthYear = birthYear; }
    public Sex getSex() { return sex; }
    public void setSex(Sex sex) { this.sex = sex; }
    public ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(ExperienceLevel experienceLevel) { this.experienceLevel = experienceLevel; }
    public BigDecimal getTypicalWeeklyDistanceKm() { return typicalWeeklyDistanceKm; }
    public void setTypicalWeeklyDistanceKm(BigDecimal typicalWeeklyDistanceKm) { this.typicalWeeklyDistanceKm = typicalWeeklyDistanceKm; }
    public BigDecimal getLongestRecentRunKm() { return longestRecentRunKm; }
    public void setLongestRecentRunKm(BigDecimal longestRecentRunKm) { this.longestRecentRunKm = longestRecentRunKm; }
    public List<String> getPreferredRunDays() { return preferredRunDays; }
    public void setPreferredRunDays(List<String> preferredRunDays) { this.preferredRunDays = preferredRunDays; }
    public Weekday getPreferredLongRunDay() { return preferredLongRunDay; }
    public void setPreferredLongRunDay(Weekday preferredLongRunDay) { this.preferredLongRunDay = preferredLongRunDay; }
    public GoalStyle getGoalStyle() { return goalStyle; }
    public void setGoalStyle(GoalStyle goalStyle) { this.goalStyle = goalStyle; }
    public Map<String, Object> getInjuryHistory() { return injuryHistory; }
    public void setInjuryHistory(Map<String, Object> injuryHistory) { this.injuryHistory = injuryHistory; }
    public int getStrengthDaysPerWeek() { return strengthDaysPerWeek; }
    public void setStrengthDaysPerWeek(int strengthDaysPerWeek) { this.strengthDaysPerWeek = strengthDaysPerWeek; }
    public Units getUnits() { return units; }
    public void setUnits(Units units) { this.units = units; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
