# 01 Product Summary

## Product Vision

Build a trustworthy adaptive running coach for adult road runners training for a half marathon or marathon. The product should feel like a structured coach, not a generic chatbot. It should produce safe deterministic plans, adapt conservatively to real-world disruptions, and explain changes clearly.

## Target Users

### Primary User Segments

- First-time half marathon runner who needs reassurance, simple structure, and clear effort guidance.
- First-time marathon runner who needs conservative progression and explicit long-run safety.
- Busy intermediate runner who wants a realistic plan that tolerates missed sessions.
- Data-trusting runner who expects adaptation decisions to be visible and explainable.

### Excluded MVP Users

- Under-18 athletes.
- Trail and ultra runners.
- Users training for multiple races at once.
- Users expecting medical, rehab, or live-coach support.

## Core Value Proposition

- Structured race plan built from deterministic coaching rules.
- Adaptation after completed, missed, partial, or overdone workouts.
- Fast fatigue and pain check-ins that directly influence training load.
- Clear explanations for what changed and why.
- Optional Strava sync that reduces manual logging without making Strava mandatory.

## MVP Goals

- Get a user from onboarding to an initial race plan in one session.
- Make today's workout obvious and easy to complete.
- Let the user record enough subjective feedback for safe adaptation.
- Adapt next-week training without creating unsafe mileage spikes.
- Explain adaptations in plain language without letting AI alter plan logic.
- Support read-only Strava import for basic workout matching.

## Non-Goals

- Replacing a qualified human coach.
- Diagnosing injuries or illnesses.
- Social motivation loops.
- Personalized route or terrain advice.
- Nutrition or hydration logging.
- Wearable ecosystem breadth.
- Subscription monetization.
- Multi-race season planning.

## Success Metrics

### Product Metrics

- Onboarding completion rate.
- Race-goal setup completion rate.
- Plan generation success rate.
- Weekly active runners with at least one workout interaction.
- Workout completion logging rate.
- Daily fatigue check-in rate among active users.
- Strava connection rate among eligible users.

### Quality Metrics

- Percentage of adaptations that surface at least one human-readable reason code.
- Percentage of plan regenerations with no rule violations.
- Percentage of workout matches accepted without manual correction.
- AI explanation validation pass rate.

### Safety Metrics

- Percentage of plans exceeding weekly progression cap should be zero.
- Percentage of adaptations increasing a long run after elevated pain should be zero.
- Percentage of high-pain check-ins that remove intensity within the same plan version should be 100 percent.

## Product Positioning

The app is not "AI that coaches you." It is "rules-first coaching with clear explanations." That positioning is safer, more defensible, and more credible to serious runners.

## Recommended MVP Default

- Default coaching tone: calm, direct, non-medical, non-hype.
- Default goal mode: finish or improve. Personal-best mode is allowed only if recent training history supports it.
