package com.company.runcoach.profile.api;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public class ProfileUpdateRequest {

    @NotEmpty
    private List<String> preferredRunDays;

    @NotNull
    private String preferredLongRunDay;

    @NotNull
    private Integer strengthDaysPerWeek;

    @NotNull
    private String units;

    private String timezone;
    private Map<String, Object> injuryHistory;
    private boolean injuryHistorySpecified;

    public List<String> preferredRunDays() {
        return preferredRunDays;
    }

    public String preferredLongRunDay() {
        return preferredLongRunDay;
    }

    public Integer strengthDaysPerWeek() {
        return strengthDaysPerWeek;
    }

    public String units() {
        return units;
    }

    public String timezone() {
        return timezone;
    }

    public Map<String, Object> injuryHistory() {
        return injuryHistory;
    }

    public boolean injuryHistorySpecified() {
        return injuryHistorySpecified;
    }

    public void setPreferredRunDays(List<String> preferredRunDays) {
        this.preferredRunDays = preferredRunDays;
    }

    public void setPreferredLongRunDay(String preferredLongRunDay) {
        this.preferredLongRunDay = preferredLongRunDay;
    }

    public void setStrengthDaysPerWeek(Integer strengthDaysPerWeek) {
        this.strengthDaysPerWeek = strengthDaysPerWeek;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @JsonSetter("injuryHistory")
    public void setInjuryHistory(Map<String, Object> injuryHistory) {
        this.injuryHistory = injuryHistory;
        this.injuryHistorySpecified = true;
    }
}
