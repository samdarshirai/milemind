# 08 Training Engine Rules

## Purpose

The training engine creates deterministic half marathon and marathon plans. It does not use AI. It does not infer hidden coaching strategies. It selects a plan scaffold and fills workouts using explicit rules.

## Input Model

Required inputs:
- Race distance: `HALF_MARATHON` or `MARATHON`
- Race date
- Experience level: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`
- Goal style: `FINISH`, `IMPROVE`, `PB`
- Preferred run days
- Preferred long-run day
- Current weekly distance over recent 4 to 6 weeks
- Longest run in past 30 days
- Injury history summary
- Strength days per week

Derived inputs:
- Weeks to race
- Available run slots per week
- Conservative baseline mileage band
- Pace anchor from target time or recent fitness input

## Plan Structure

Phases:
- Base
- Build
- Race Specific
- Taper

Recommended MVP default:
- No separate post-race recovery phase in the active plan. Recovery begins after race day and can be handled later.

## Eligible Training Frequency

- Half marathon minimum: 3 run days per week
- Half marathon maximum in MVP: 5 run days per week
- Marathon minimum: 4 run days per week
- Marathon maximum in MVP: 6 run days per week

Reject plans outside these bounds.

## Half Marathon Rules

### Block Length

- Beginner: 12 to 16 weeks
- Intermediate: 10 to 14 weeks
- Advanced: 10 to 14 weeks

If race date allows a longer runway, cap active generation at the nearest supported block and start later or add extra base weeks with repeated low-risk structure.

### Weekly Structure

- 3 days: 1 long run, 1 quality session, 1 easy run
- 4 days: 1 long run, 1 quality session, 2 easy or recovery runs
- 5 days: 1 long run, 1 quality session, 3 easy or recovery runs

### Quality Sessions

- Beginner: threshold-lite, fartlek, short hill reps, strides
- Intermediate: threshold, cruise intervals, progression runs
- Advanced: threshold and selective race-pace segments

### Long Run Progression

- Increase long run by 1 to 2 km in build weeks.
- Insert stepback long run every 3rd or 4th week.
- Peak long run:
  - Beginner: 16 to 18 km
  - Intermediate: 18 to 21 km
  - Advanced: 18 to 22 km

### Weekly Mileage Progression

- Build weeks: increase total planned weekly distance by 4 to 8 percent.
- Absolute ceiling: never exceed 10 percent week-over-week.
- Recovery week: reduce 15 to 25 percent from prior build week.

## Marathon Rules

### Block Length

- Beginner: 16 to 20 weeks
- Intermediate: 14 to 18 weeks
- Advanced: 14 to 18 weeks

### Weekly Structure

- 4 days: 1 long run, 1 quality session, 2 easy runs
- 5 days: 1 long run, 1 quality session, 3 easy or recovery runs
- 6 days: 1 long run, 1 primary quality session, 1 secondary light quality or steady session every other week, remaining easy runs

### Quality Sessions

- Beginner: mostly threshold-lite and marathon-effort steady work
- Intermediate: threshold, marathon-pace blocks, occasional hill work
- Advanced: threshold, marathon-pace blocks, selective VO2 every 2 weeks in early build only

### Long Run Progression

- Increase long run by 2 to 3 km in build weeks when safe.
- Stepback long run every 3rd or 4th week.
- Peak long run:
  - Beginner: 28 to 30 km
  - Intermediate: 30 to 32 km
  - Advanced: 32 to 34 km

### Weekly Mileage Progression

- Build weeks: increase total planned weekly distance by 4 to 8 percent.
- Absolute ceiling: never exceed 10 percent week-over-week.
- Recovery week: reduce 15 to 25 percent.

## Weekly Mileage Progression Rules

- Base first weekly target on recent average weekly distance, not aspirational goal.
- If input history is sparse, anchor lower.
- If prior injury history exists, cap build weeks at 6 percent.
- If current weekly distance is below minimum safe start for chosen race, reject goal date or extend runway instead of spiking mileage.

Recommended MVP default:
- Use the lower of:
  - reported weekly distance
  - inferred safe weekly distance from longest recent run times 3

## Long-Run Progression Rules

- Long run cannot exceed 35 percent of weekly distance for half marathon plans.
- Long run cannot exceed 40 percent of weekly distance for marathon plans.
- Long run cannot exceed 110 percent of the runner's longest run in the previous 30 days during initial build weeks.
- Long run cannot exceed the applicable duration cap.

Duration caps:
- Half beginner: 75 to 140 minutes
- Half intermediate: 90 to 150 minutes
- Marathon beginner: 90 to 210 minutes
- Marathon intermediate: 105 to 225 minutes
- Marathon advanced: 120 to 240 minutes

## Recovery Week Rules

- Default cadence: every 4th week.
- Move recovery week earlier if:
  - two key workouts in 10 days are under-completed
  - fatigue state is orange or red
  - pain severity is 5 or higher
  - illness flag is true

Recovery week behavior:
- Reduce weekly volume 15 to 25 percent.
- Reduce long-run volume 10 to 20 percent.
- Keep at most one moderate quality session.

## Taper Logic

### Half Marathon

- 2-week taper
- Week -2: reduce volume 20 percent, preserve one short quality session
- Race week: reduce volume 35 to 45 percent, no heavy long run, keep short strides or short race-pace touches

### Marathon

- 3-week taper
- Week -3: reduce volume 15 to 20 percent
- Week -2: reduce volume 25 to 30 percent
- Race week: reduce volume 40 to 50 percent

Taper rules:
- Reduce volume more than intensity.
- Do not add compensatory mileage in taper.
- Long run in final 2 weeks must not exceed conservative caps.

## Workout Type Distribution

### Half Marathon

- Easy and recovery: 65 to 80 percent
- Long run: 15 to 25 percent
- Quality work: 10 to 15 percent

### Marathon

- Easy and recovery: 70 to 82 percent
- Long run: 18 to 28 percent
- Quality work: 8 to 15 percent

Interpretation:
- Distribution is by weekly training stress and total volume, not raw workout count only.

## Pace And Effort Zones

Recommended MVP default:
- Use effort-first guidance with optional pace anchors.

Zones:
- `Z1_RECOVERY`: RPE 2 to 3, conversational, very easy
- `Z2_EASY`: RPE 3 to 4, controlled easy aerobic
- `Z3_STEADY_THRESHOLD`: RPE 6 to 7, comfortably hard
- `Z4_INTERVAL`: RPE 7.5 to 8.5, hard but repeatable
- `Z5_STRIDES`: short fast relaxed efforts, not maximal

Rules:
- If target time exists, calculate pace bands from projected race pace.
- If target time does not exist, use time and RPE prescriptions only.
- Never require exact heart-rate control in MVP.

## Session Spacing Rules

- No more than two quality stressors in any rolling 4-day window.
- Long run and primary quality workout must not be on adjacent days for beginner and intermediate runners.
- The day after a long run must be rest, recovery, or easy only.
- Do not schedule VO2 work in the same week as the biggest long-run increase.

## Safety Caps

- No more than 10 percent weekly mileage increase.
- No long-run increase above 10 percent over recent longest run when risk is elevated.
- No more than one VO2-oriented session every 14 days for beginner marathon users.
- Remove VO2 entirely for users with pain, illness, or repeated incompletions.
- No back-to-back quality days.
- No plan generation if minimum runway to race is below allowed threshold.

## Plan Output Schema

Each planned workout should include:
- `workoutType`
- `workoutSubtype`
- `scheduledDate`
- `plannedDistanceKm` or `plannedDurationMin`
- `intensityZone`
- `structure`
- `rationale`
- `planVersion`

## Pseudocode

```text
validateUserEligibility()
deriveWeeksToRace()
deriveSafeBaselineMileage()
selectDistanceTemplate(distance, experienceLevel, weeksToRace)
selectWeeklyFrequency(preferredRunDays, distance)
createMacroPhaseCalendar()

for each week in block:
    if shouldBeRecoveryWeek(week, priorSignals):
        targetWeeklyVolume = reduceVolume(previousBuildWeek)
    else:
        targetWeeklyVolume = progressVolume(previousWeek, baseline, caps)

    longRun = buildLongRun(distance, experienceLevel, recentLongestRun, targetWeeklyVolume, caps)
    qualitySession = buildPrimaryQuality(distance, phase, experienceLevel, goalStyle)
    secondarySessions = fillEasyRuns(targetWeeklyVolume, availableDays, spacingRules)

    enforceSpacingRules()
    enforceDistributionRules()
    enforceSafetyCaps()
    persistWeekAndWorkouts()

return trainingPlan
```

## Things The Engine Must Not Do

- Do not use AI outputs.
- Do not generate double long-run weeks.
- Do not make up missed volume during generation.
- Do not prescribe race-pace-heavy plans to low-history beginners.
- Do not assume every runner can absorb six-day plans.
