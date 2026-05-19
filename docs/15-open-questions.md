# 15 Open Questions

## Missing Product Decisions

- Should the app allow a user with insufficient training history to create a plan with a later race date automatically suggested?
- Should goal style `PB` be hidden for beginner users in MVP?
- Should strength sessions appear as first-class workouts or only as optional guidance notes in MVP?
- Should rest days appear as explicit calendar cards or only as empty informational states?

## Missing Technical Decisions

- Exact JWT expiry and refresh rotation policy.
- Whether Redis is used in MVP for job coordination or deferred.
- Whether JPA alone is sufficient for read-heavy calendar queries or whether projection queries should be introduced early.
- Exact encryption mechanism for Strava tokens and refresh tokens.
- Final choice of DeepSeek SDK or plain HTTP client wrapper.

## Risks

- Plan logic may still feel too generic if workout library depth is too shallow.
- Adaptation may feel opaque if reason-code to explanation mapping is weak.
- Strava legal retention requirements may force tighter deletion behavior than initially expected.
- AI explanations may feel repetitive if prompt templates are too narrow.
- Android offline limitations may frustrate users expecting full offline workout logging.

## Things To Decide Before Coding

- Final brand package name and app link domains.
- Auth approach confirmation.
- Final enum sets for workout types, phases, and reason codes.
- Exact minimum runway rules by distance.
- Legal-approved Strava data retention period.
- Final DeepSeek model configuration strategy.

## Things That Can Be Deferred

- Payments
- Social features
- Multi-goal season planning
- Wearable integrations beyond Strava
- Strength content library
- Personalized notifications
- Rich admin tooling
- Coach marketplace

## Recommended MVP Defaults Where PRD Was Broad

- Hide advanced optimization language from beginner flows.
- Use effort-based prescriptions when pace certainty is weak.
- Limit adaptation scope to 7 to 14 days, not full-block rewrites.
- Use one AI model path and one validator path.
- Keep read caching simple and skip offline mutation queue.

## Challenges To Broad Or Unsafe PRD Ideas

- Any suggestion that AI should build or rewrite plans should be rejected.
- Any suggestion that missed mileage should be made up later should be rejected.
- Any suggestion that the app should infer medical conditions should be rejected.
- Any suggestion that Strava should become the permanent unrestricted system of record should be rejected.
