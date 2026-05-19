# 00 Implementation Pack Index

## Overview

This documentation pack converts the research PRD into an implementation-ready MVP for a native Android adaptive running coach.

Stack:
- Android app: Kotlin, Jetpack Compose, MVVM
- Backend: Java Spring Boot modular monolith
- Database: PostgreSQL
- AI: DeepSeek, backend-only, explanation-only
- Integration: Strava read-only sync

Core product rule:
- The backend owns all training logic, plan generation, plan adaptation, Strava token handling, and DeepSeek calls.
- The Android app is a client only. It renders data, collects user input, opens OAuth flows, and calls backend APIs.

## Reading Order

1. [01-product-summary.md](/Users/ronalisenapati/Documents/milemind/docs/01-product-summary.md)
2. [02-mvp-scope.md](/Users/ronalisenapati/Documents/milemind/docs/02-mvp-scope.md)
3. [12-safety-guardrails.md](/Users/ronalisenapati/Documents/milemind/docs/12-safety-guardrails.md)
4. [08-training-engine-rules.md](/Users/ronalisenapati/Documents/milemind/docs/08-training-engine-rules.md)
5. [09-adaptation-engine-rules.md](/Users/ronalisenapati/Documents/milemind/docs/09-adaptation-engine-rules.md)
6. [05-backend-architecture.md](/Users/ronalisenapati/Documents/milemind/docs/05-backend-architecture.md)
7. [06-database-schema.md](/Users/ronalisenapati/Documents/milemind/docs/06-database-schema.md)
8. [07-api-contracts.md](/Users/ronalisenapati/Documents/milemind/docs/07-api-contracts.md)
9. [03-android-app-spec.md](/Users/ronalisenapati/Documents/milemind/docs/03-android-app-spec.md)
10. [04-screen-navigation.md](/Users/ronalisenapati/Documents/milemind/docs/04-screen-navigation.md)
11. [10-strava-integration.md](/Users/ronalisenapati/Documents/milemind/docs/10-strava-integration.md)
12. [11-deepseek-ai-integration.md](/Users/ronalisenapati/Documents/milemind/docs/11-deepseek-ai-integration.md)
13. [13-implementation-slices.md](/Users/ronalisenapati/Documents/milemind/docs/13-implementation-slices.md)
14. [14-test-plan.md](/Users/ronalisenapati/Documents/milemind/docs/14-test-plan.md)
15. [15-open-questions.md](/Users/ronalisenapati/Documents/milemind/docs/15-open-questions.md)
16. [16-screen-design-prompts.md](/Users/ronalisenapati/Documents/milemind/docs/16-screen-design-prompts.md)
17. [17-agent-workflow.md](/Users/ronalisenapati/Documents/milemind/docs/17-agent-workflow.md)

## MVP Guardrails

- Adult runners only.
- Road running only.
- Half marathon and marathon only.
- One active race goal per user.
- Deterministic training plans only.
- Deterministic adaptation only.
- AI may explain decisions but may not create or edit plans.
- Strava is read-only and optional.
- Manual workout completion must work without Strava.
- Safety defaults are conservative when data is missing or risk signals are elevated.

## What Not To Build

- No social feed.
- No route planning.
- No nutrition tracking.
- No smartwatch app.
- No payments in MVP.
- No ultra plans.
- No multi-goal season planner.
- No coach marketplace.
- No free-form AI plan generation.
- No medical diagnosis or injury treatment advice.
- No offline-first mutation queue in MVP.
- No multi-tenant backend complexity.
- No microservices split in MVP.

## Architecture Principles

### Backend Owns Training Logic

- Plan generation runs only in the backend training engine.
- Adaptation decisions run only in the backend adaptation engine.
- Strava activity matching runs only in the backend.
- DeepSeek prompts are built only in the backend.
- Safety caps are enforced only in the backend.

### Android App Is Client Only

- Android never computes a plan.
- Android never recomputes adaptation locally.
- Android never stores Strava access tokens.
- Android never calls DeepSeek directly.
- Android only caches backend read models for UX and resilience.

### Rules Before AI

- Rules create plan state.
- Events update runner state.
- Deterministic adaptation changes future workouts.
- AI converts approved facts into user-facing explanations.

### Recommended MVP Defaults

- Auth: email plus password with JWT access token and refresh token.
- Plan time horizon: full race block generated at goal creation.
- Adaptation horizon: next 7 to 14 days only.
- Calendar granularity: day and week views only.
- Notification strategy: push notifications can be deferred after MVP unless already available in the app shell.
