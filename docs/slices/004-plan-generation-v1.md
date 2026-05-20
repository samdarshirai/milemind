# Slice 004 - Plan Generation V1

Status: Completed

Implemented scope:
- `POST /v1/plans/generate`
- `GET /v1/plans/{planId}`
- `GET /v1/plans/current`
- Flyway migration for `training_plan`, `training_plan_week`, `planned_workout`
- Deterministic backend plan generation based on profile + race goal
- Duplicate generation behavior: existing active/generated plan for same race goal is returned unless `forceRegenerate=true`

Deterministic rules implemented:
- Week-by-week progression from `startDate` to `raceDate`
- Volume progression by experience level with 10% cap
- Recovery week every 4th week
- 2-week taper (`~70%` then `~45%`)
- Workouts only on preferred run days
- Long run prioritized on preferred long-run day, otherwise latest available day
- Beginner-friendly quality handling (no hard interval bias)

Validation added:
- runner profile required
- race goal required and ownership enforced
- race goal must be active
- race date must be future
- start date must be before race date
- preferred run days required
- non-negative weekly distance required
- supported race distance required (half/marathon)

Out-of-scope retained:
- No AI generation (DeepSeek not used)
- No adaptation/regeneration logic beyond explicit `forceRegenerate`
- No Strava sync
