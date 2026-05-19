# 09 Adaptation Engine Rules

## Purpose

The adaptation engine modifies near-term plan state based on actual execution and risk signals. It is deterministic and auditable. It must prefer conservative consistency over aggressive optimization.

## Evidence Inputs

- Completed workout data
- Skipped workout events
- Rescheduled workout events
- Fatigue signals
- Pain and injury feedback
- Recent long-run history
- Current plan version
- Recent adaptation history

## Core Output

The engine outputs an `adaptation_decision` and optionally a regenerated next 7 to 14 days of workouts.

## Completed Workout Handling

### Normal Completion

If completion score is 0.8 or above and no high-risk feedback exists:
- Mark workout completed.
- Keep plan structure unchanged.
- Update quality and adherence aggregates.

### Strong Completion

If completion score is 0.95 or above and quality score is high:
- Do not immediately increase future workload.
- Record readiness evidence only.
- Allow later pace-tightening only if repeated evidence supports it.

Reason:
- MVP must not reward one strong day with an unsafe progression spike.

## Missed Workout Handling

### Missed Easy Run

- Default action: drop the workout.
- No make-up volume.
- No plan regeneration unless fatigue or schedule disruption pattern is significant.

### Missed Quality Workout

- Allow one reschedule if a safe gap remains before long run and before next quality session.
- Otherwise drop it.
- Never stack missed quality on top of next planned quality.

### Missed Long Run

- Allow move by one day only if:
  - the next day is free
  - no adjacent quality workout results
  - user risk state is green or yellow
- Otherwise shorten and move within same week or skip.
- Do not push full missed long-run load into next week.

## Partial Completion Handling

Definition:
- Completion score from 0.4 to 0.79.

Rules:
- Treat one partial key workout as caution, not failure.
- Treat two partial key workouts in 10 days as a signal to reduce next 7 days.
- If partial completion is due to pain or illness, trigger safety branch immediately.

## Overdone Workout Handling

Definition:
- Actual distance or duration materially exceeds prescription.

Recommended MVP thresholds:
- More than 20 percent over planned duration for easy or long run
- More than 15 percent over planned volume for quality workout

Rules:
- Mark completion as overdone.
- Reduce next easy run volume or replace with recovery.
- Do not reward over-compliance with future increases.
- If overdone long run exceeds safety cap, flag overload risk and protect next 7 days.

## Fatigue Check-In Handling

### Readiness State Mapping

- Green: low soreness, acceptable sleep, manageable stress, okay motivation
- Yellow: mild warning but manageable
- Orange: multiple weak signals or one strong fatigue signal
- Red: severe fatigue, illness, or major performance drop indicators

### Actions

- Green: no structural adaptation
- Yellow: monitor only, optional small pace softening
- Orange: reduce next quality stress, keep only one key workout in next 7 days
- Red: convert current week to recovery structure, remove intensity

## Pain Check-In Handling

### Severity Rules

- 0 to 2: note only, no automatic change unless repeated
- 3 to 4: caution, remove optional intensity if localized
- 5 to 6: reduce volume and remove faster work for at least 3 to 7 days
- 7 or above: remove all intensity for 7 days and surface professional-care guidance copy

### Pain Type Rules

- Sharp localized pain is treated more conservatively than diffuse soreness.
- Pain during run is treated more conservatively than post-run stiffness.
- Inability to continue running forces red safety state.

## Plan Regeneration Rules

Regeneration horizon:
- Next 7 days for mild changes
- Next 14 days for downshift week or illness/pain cases

Allowed changes:
- Replace quality with easy run
- Shorten next long run
- Insert rest day
- Convert current week to recovery week
- Soften pace targets
- Move one workout within safe spacing

Disallowed changes:
- Increasing weekly distance after orange or red readiness
- Increasing long-run distance after pain report
- Adding an extra quality workout
- Rewriting the full plan from scratch for minor events

## Reason Codes

Required machine-readable reason codes:
- `WORKOUT_COMPLETED_AS_PLANNED`
- `WORKOUT_UNDER_COMPLETED`
- `WORKOUT_OVERDONE`
- `MISSED_EASY_RUN`
- `MISSED_QUALITY_RUN`
- `MISSED_LONG_RUN`
- `HIGH_FATIGUE_SCORE`
- `ILLNESS_FLAG`
- `PAIN_SIGNAL_PRESENT`
- `SHARP_LOCALIZED_PAIN`
- `PRESERVE_LONG_RUN`
- `REDUCE_INTENSITY`
- `INSERT_RECOVERY_WEEK`
- `PROTECT_CONSISTENCY`
- `STALE_PLAN_VERSION_RETRY`

## Adaptation Decision Schema

```json
{
  "decisionId": "uuid",
  "trainingPlanId": "uuid",
  "planVersionBefore": 2,
  "planVersionAfter": 3,
  "decisionType": "DOWNSHIFT_WEEK",
  "decisionScope": "WEEK",
  "confidence": 0.93,
  "reasonCodes": [
    "HIGH_FATIGUE_SCORE",
    "PAIN_SIGNAL_PRESENT",
    "PROTECT_CONSISTENCY"
  ],
  "changes": [
    {
      "plannedWorkoutId": "uuid",
      "changeType": "REPLACE",
      "fromWorkoutType": "THRESHOLD",
      "toWorkoutType": "EASY",
      "fromDurationMin": 70,
      "toDurationMin": 45
    }
  ]
}
```

## Decision Policy

### Keep Plan Unchanged

Use when:
- normal completion
- isolated missed easy run
- yellow readiness without performance drop

### Minor Softening

Use when:
- one partial key workout
- overdone session
- yellow readiness with elevated soreness

### Downshift Next 7 Days

Use when:
- orange readiness
- two under-completed key workouts in 10 days
- missed long run plus rising fatigue

### Recovery Week Conversion

Use when:
- illness flag
- red readiness
- pain severity 5 or higher with localized pain

## Deterministic Scoring Defaults

Recommended MVP default:
- `completionScore`
  - 1.0 if within 90 to 110 percent of planned duration or distance and workout type intent preserved
  - 0.7 if 70 to 89 percent completed
  - 0.4 if 40 to 69 percent completed
  - 0.0 if missed

- `fatigueRisk`
  - sum weighted factors from soreness, sleep, stress, motivation, recent partials, and long-run spike risk
  - map to 0 to 100 scale

## Pseudocode

```text
loadCurrentPlan(planId, planVersion)
loadRecentCompletions(userId, last14Days)
loadRecentFatigueSignals(userId, last7Days)
loadRecentPainSignals(userId, last14Days)

if painSeverity >= 7 or sharpLocalizedPain and canRun == false:
    decision = removeIntensityFor7Days()
    regenerateNext14Days(decision)
    saveDecisionAndNewPlanVersion()
    return

if illnessFlag:
    decision = convertCurrentWeekToRecovery()
    regenerateNext14Days(decision)
    saveDecisionAndNewPlanVersion()
    return

fatigueRisk = calculateFatigueRisk()
underCompletedKeyWorkouts = countUnderCompletedKeyWorkouts(last10Days)

if fatigueRisk >= 70:
    decision = downshiftNext7Days()
elif underCompletedKeyWorkouts >= 2:
    decision = softenPacesAndReduceVolume()
elif missedLongRun and readinessIsNotGreen():
    decision = protectNextLongRunAndReduceLoad()
elif overdoneWorkout:
    decision = insertRecoveryAdjustment()
else:
    decision = noStructuralChange()

applyDecisionWithinSafetyCaps()
incrementPlanVersionIfChanged()
persistDecision()
emitAdaptationEvent()
```

## Engine Constraints

- Every change must preserve future workout ordering semantics.
- Every regenerated workout must pass training engine safety caps.
- Every decision must have reason codes.
- Every change must be reproducible from the same inputs.
- AI explanation is optional and downstream, never part of decisioning.
