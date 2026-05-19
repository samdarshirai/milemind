# 17 Agent Workflow

## Purpose

This file defines how coding agents should work on the adaptive running coach project.

The goal is to keep implementation controlled, slice-based, testable, and aligned with the product's core architecture:

- Kotlin + Jetpack Compose Android app
- Java Spring Boot modular monolith backend
- PostgreSQL database
- deterministic training engine
- deterministic adaptation engine
- Strava read-only integration
- DeepSeek explanation-only AI

Agents must not treat the PRD as permission to build everything at once.

## Source Of Truth Documents

Agents must read documents in this order before implementation:

1. `00-implementation-pack-index.md`
2. `01-product-summary.md`
3. `02-mvp-scope.md`
4. `12-safety-guardrails.md`
5. `13-implementation-slices.md`
6. The specific domain document for the slice:
   - Android: `03-android-app-spec.md`
   - Screens: `04-screen-navigation.md`
   - Backend: `05-backend-architecture.md`
   - Database: `06-database-schema.md`
   - APIs: `07-api-contracts.md`
   - Training: `08-training-engine-rules.md`
   - Adaptation: `09-adaptation-engine-rules.md`
   - Strava: `10-strava-integration.md`
   - AI: `11-deepseek-ai-integration.md`
7. `14-test-plan.md`
8. `15-open-questions.md`

For screen design work, also read:

- `16-screen-design-prompts.md`

## Golden Rules

- Implement one slice at a time.
- Do not build features outside the active slice.
- Do not rewrite architecture without creating or updating a decision document.
- Backend owns all training logic.
- Backend owns all adaptation logic.
- Backend owns Strava token handling.
- Backend owns DeepSeek calls.
- Android is a client only.
- Android may validate forms but must not calculate training plans.
- AI may explain approved state but must not create or edit plans.
- Strava is read-only in MVP.
- Safety guardrails cannot be bypassed.
- If uncertain, choose the safer and smaller implementation.

## Mandatory Architecture Boundaries

### Android Must Not

- Generate training plans.
- Regenerate plans.
- Calculate fatigue risk beyond displaying backend result.
- Decide adaptation.
- Store Strava access or refresh tokens.
- Call DeepSeek directly.
- Persist authoritative training state.
- Invent workout rationale.
- Hide backend safety warnings.

### Backend Must

- Validate all user inputs.
- Generate all plans.
- Enforce all training safety caps.
- Decide all adaptations.
- Persist adaptation decisions with reason codes.
- Own Strava OAuth callback and token refresh.
- Own DeepSeek prompt construction and validation.
- Return deterministic fallback text when AI fails.
- Reject stale plan mutations where required.

### AI Must Not

- Generate plans.
- Edit plans.
- Override safety rules.
- Diagnose injury or illness.
- Tell a user to push through severe pain.
- Use Strava data for model training.
- Make performance guarantees.

## Recommended Agent Roles

### 1. Product Slice Planner

Use when starting a new slice.

Responsibilities:

- convert a slice into a detailed implementation plan;
- identify backend tasks;
- identify Android tasks;
- identify database changes;
- identify API changes;
- identify tests;
- identify edge cases;
- identify out-of-scope items.

Must not:

- write production code;
- expand MVP scope;
- add features not in docs.

### 2. Backend Spring Boot Developer

Use for backend implementation.

Responsibilities:

- entities;
- repositories;
- services;
- controllers;
- DTOs;
- Flyway migrations;
- domain policies;
- integration tests;
- API contract alignment.

Must follow:

- Java Spring Boot modular monolith;
- thin controllers;
- service/domain-owned logic;
- no direct controller-to-repository business flow;
- no leaking entities to API responses.

### 3. Android Kotlin Compose Developer

Use for Android implementation.

Responsibilities:

- Compose screens;
- ViewModels;
- UI state models;
- repositories;
- network DTO mapping;
- navigation;
- local read cache;
- error/loading/empty states.

Must follow:

- Kotlin only;
- Jetpack Compose only;
- MVVM;
- Material 3;
- repository boundary;
- backend as source of truth.

### 4. Training Engine Specialist

Use for plan-generation work.

Responsibilities:

- plan templates;
- phase construction;
- mileage progression;
- long-run policy;
- recovery weeks;
- taper logic;
- workout distribution;
- safety caps;
- deterministic tests.

Must not:

- use AI;
- write UI code;
- introduce advanced plans outside MVP;
- compress unsafe race timelines.

### 5. Adaptation Engine Specialist

Use for adaptation work.

Responsibilities:

- completed workout handling;
- missed workout handling;
- partial completion handling;
- overdone workout handling;
- fatigue and pain handling;
- plan versioning;
- decision audit;
- reason codes;
- regeneration horizon.

Must not:

- allow catch-up stacking;
- increase load after high pain;
- make hidden plan changes;
- call AI for decisioning.

### 6. Strava Integration Specialist

Use for Strava slices.

Responsibilities:

- connect session;
- OAuth state validation;
- backend callback;
- encrypted token storage;
- token refresh;
- webhook verification;
- webhook ingest;
- activity import;
- activity matching;
- disconnect/deauthorization.

Must not:

- store Strava tokens on Android;
- write workouts to Strava in MVP;
- use Strava data for AI training;
- ignore idempotency.

### 7. DeepSeek AI Specialist

Use for AI explanation slices.

Responsibilities:

- prompt templates;
- DeepSeek client abstraction;
- JSON schema validation;
- safety filtering;
- deterministic fallback;
- coach chat boundaries;
- AI audit persistence.

Must not:

- mutate plan state;
- decide training changes;
- diagnose injury;
- allow unvalidated output to display.

### 8. Code Reviewer / Architecture Guardian

Use after every implementation pass.

Responsibilities:

- verify slice scope;
- verify architecture boundaries;
- verify tests;
- verify API consistency;
- verify safety rules;
- verify Android/backend separation;
- identify hidden overbuild;
- block unsafe behavior.

### 9. QA/Test Agent

Use after a slice is integrated.

Responsibilities:

- test plan extraction;
- backend test review;
- Android test review;
- manual QA checklist;
- regression risk list;
- acceptance criteria status.

## Slice Execution Workflow

For each slice in `13-implementation-slices.md`, use this workflow.

### Step 1: Slice Planning

Input:

- active slice from `13-implementation-slices.md`;
- relevant architecture docs;
- relevant API docs;
- relevant safety/rule docs.

Agent output:

- implementation plan;
- files likely to change;
- sequence of work;
- risks;
- edge cases;
- test checklist.

Prompt template:

```text
Read the implementation pack and prepare a detailed implementation plan for Slice [N]: [slice name].

Use:
- docs/00-implementation-pack-index.md
- docs/13-implementation-slices.md
- all domain docs relevant to this slice

Return:
- goal
- scope
- out of scope
- backend work
- Android work
- DB changes
- API changes
- tests
- edge cases
- files likely to change
- acceptance criteria checklist

Do not write production code.
Do not expand scope beyond this slice.
```

### Step 2: Backend Implementation

Use only if the slice has backend tasks.

Prompt template:

```text
Implement the backend part of Slice [N]: [slice name].

Read:
- docs/00-implementation-pack-index.md
- docs/05-backend-architecture.md
- docs/06-database-schema.md
- docs/07-api-contracts.md
- docs/13-implementation-slices.md
- domain docs relevant to this slice
- docs/14-test-plan.md

Rules:
- Java Spring Boot modular monolith
- controllers stay thin
- business logic belongs in service/domain/policy classes
- use Flyway for schema changes
- add tests required by the slice
- do not implement Android code
- do not implement features outside this slice
- preserve backend ownership of training/adaptation logic

After implementation, return:
- summary
- files changed
- tests added
- how to run tests
- acceptance criteria status
- known limitations
```

### Step 3: Backend Review

Prompt template:

```text
Review the backend implementation for Slice [N]: [slice name].

Check:
- slice scope
- architecture boundaries
- DB migration safety
- API contract correctness
- security
- test coverage
- idempotency where relevant
- plan/adaptation safety where relevant
- no AI decisioning
- no client-owned training logic

Return:
- pass/fail
- blocking issues
- non-blocking issues
- recommended fixes
- missing tests
```

### Step 4: Android Implementation

Use only if the slice has Android tasks.

Prompt template:

```text
Implement the Android part of Slice [N]: [slice name].

Read:
- docs/03-android-app-spec.md
- docs/04-screen-navigation.md
- docs/07-api-contracts.md
- docs/13-implementation-slices.md
- docs/16-screen-design-prompts.md when UI design is involved
- docs/14-test-plan.md

Rules:
- Kotlin only
- Jetpack Compose only
- MVVM
- StateFlow-based UI state
- repository layer wraps API calls
- backend is source of truth
- Android must not calculate plans or adaptations
- support loading, empty, error, and success states
- do not implement features outside this slice

After implementation, return:
- summary
- files changed
- tests added
- how to run tests
- screenshots or screen notes if applicable
- acceptance criteria status
- known limitations
```

### Step 5: Android Review

Prompt template:

```text
Review the Android implementation for Slice [N]: [slice name].

Check:
- screen requirements from docs/04-screen-navigation.md
- Android architecture from docs/03-android-app-spec.md
- API contract usage
- loading, empty, error, success states
- no local training/adaptation logic
- token and Strava security boundaries
- Compose state management
- accessibility basics
- tests

Return:
- pass/fail
- blocking issues
- non-blocking issues
- missing UI states
- recommended fixes
```

### Step 6: Full Slice Integration Review

Prompt template:

```text
Perform a full integration review for Slice [N]: [slice name].

Check:
- backend and Android API compatibility
- DB migration and DTO consistency
- acceptance criteria from docs/13-implementation-slices.md
- tests from docs/14-test-plan.md
- safety requirements from docs/12-safety-guardrails.md
- no scope creep
- no duplicated business logic on Android

Return:
- pass/fail
- blocking issues
- manual QA checklist
- regression risks
- follow-up tasks
```

## Required Agent Output Format

Every implementation agent must end with:

```text
Summary:
- ...

Files changed:
- ...

Tests added or updated:
- ...

How to run:
- ...

Acceptance criteria:
- [x] ...
- [ ] ...

Risks / follow-ups:
- ...
```

Every review agent must end with:

```text
Verdict:
- PASS / FAIL

Blocking issues:
- ...

Non-blocking issues:
- ...

Missing tests:
- ...

Recommended next action:
- ...
```

## Branch And Commit Workflow

Recommended branch naming:

```text
slice-[number]-[short-name]
```

Examples:

```text
slice-01-auth-session
slice-04-plan-generation-v1
slice-07-adaptation-engine-v1
```

Recommended commit style:

```text
feat(auth): add register and login endpoints
feat(android-auth): add sign in and sign up screens
test(planning): cover marathon taper generation
fix(adaptation): prevent missed long-run catch-up stacking
```

## Documentation Update Rules

Agents must update docs when implementation intentionally changes behavior.

Update required when:

- API request/response changes;
- DB schema changes;
- enum values change;
- reason codes change;
- training/adaptation rule changes;
- screen navigation changes;
- AI prompt schema changes;
- Strava flow changes.

Do not update docs to hide implementation mistakes. Fix the code instead.

## Testing Rules By Slice Type

### Auth Slices

Required tests:

- registration success;
- duplicate email;
- invalid login;
- refresh rotation;
- expired refresh token;
- Android session restore.

### Onboarding/Profile Slices

Required tests:

- adult-only validation;
- required fields;
- preferred day validation;
- timezone persistence;
- Android form validation;
- onboarding success flow.

### Training Engine Slices

Required tests:

- half marathon beginner shape;
- marathon beginner shape;
- recovery week insertion;
- taper placement;
- weekly mileage cap;
- long-run cap;
- spacing rules;
- invalid race date rejection.

### Workout Execution Slices

Required tests:

- completion scoring;
- duplicate completion prevention;
- skip handling;
- reschedule validation;
- stale plan version conflict.

### Check-In And Safety Slices

Required tests:

- fatigue readiness mapping;
- pain severity mapping;
- high pain removes intensity;
- illness triggers downshift;
- user-facing safety copy does not diagnose.

### Adaptation Slices

Required tests:

- missed easy run drops without catch-up;
- missed quality run reschedules only when safe;
- missed long run does not overload next week;
- partial completion repeated signal;
- overdone workout reduces next load;
- orange readiness downshifts;
- red readiness removes intensity.

### Strava Slices

Required tests:

- OAuth state validation;
- token encryption;
- refresh token rotation;
- webhook idempotency;
- activity import upsert;
- delete event handling;
- deauthorization cleanup;
- low-confidence match review.

### AI Slices

Required tests:

- valid JSON accepted;
- invalid JSON rejected;
- missing reason code rejected;
- medical diagnosis rejected;
- unsafe escalation rejected;
- deterministic fallback used;
- AI does not mutate plan.

## Manual QA Flow For MVP

Before calling the MVP ready, run this complete manual flow:

1. Fresh install.
2. Sign up.
3. Complete onboarding for half marathon.
4. Generate plan.
5. View Today.
6. View Plan Calendar.
7. Open Workout Detail.
8. Complete workout manually.
9. Submit fatigue check-in.
10. Submit high-pain check-in.
11. Confirm plan downshifts.
12. Confirm `What changed?` appears.
13. Connect Strava.
14. Import or simulate Strava activity.
15. Confirm matching or import review.
16. Request workout explanation.
17. Request adaptation explanation.
18. Try unsafe AI question.
19. Confirm safe boundary response.
20. Sign out and restore session.

## Pull Request Checklist

Every PR must answer:

- Which slice does this implement?
- Which docs were used?
- What changed?
- What tests were added?
- What safety rules are affected?
- Does Android duplicate any backend logic?
- Does AI alter plan state?
- Does Strava remain read-only?
- Are loading, empty, error, and success states handled?
- Are docs updated if behavior changed?

## Definition Of Done

A slice is done only when:

- implementation matches slice scope;
- backend tests pass;
- Android tests pass where applicable;
- API contracts are honored;
- DB migrations are repeatable;
- safety rules are preserved;
- no forbidden behavior is introduced;
- manual QA for the slice passes;
- reviewer has no blocking issues.

## Forbidden Behaviors

Agents must not:

- build social features;
- build payments in MVP;
- build route planning;
- build wearable integrations beyond Strava;
- add ultra/trail plans;
- create a generic chatbot coach;
- let DeepSeek write plans;
- let Android calculate training logic;
- make up missed mileage by stacking workouts;
- suggest pushing through severe pain;
- store Strava tokens on Android;
- use Strava data to train AI;
- create microservices for MVP;
- add unnecessary architecture layers.

## Handling Open Questions

When an agent hits an open question:

1. Check `15-open-questions.md`.
2. If a recommended MVP default exists, use it.
3. If no default exists, choose the smallest safe implementation.
4. Document the assumption in the implementation summary.
5. Do not block the slice unless the decision is legally or safety critical.

Examples:

- If Redis is undecided, use DB-backed outbox polling first.
- If pace certainty is weak, use effort-based prescriptions.
- If PB support is ambiguous for beginners, hide PB for beginners.
- If Strava retention is legally unresolved, keep raw payload retention minimal.

## Recommended Slice Order

Follow the order in `13-implementation-slices.md`:

1. Auth And Session Foundation
2. Onboarding And Profile
3. Race Goal Setup
4. Deterministic Plan Generation V1
5. Workout Detail And Manual Completion
6. Fatigue And Pain Check-Ins
7. Adaptation Engine V1
8. Progress And Insights
9. Strava OAuth And Connection Status
10. Strava Webhook Import
11. Strava Match To Planned Workouts
12. DeepSeek Explanations
13. Hardening And Observability

Do not start with Strava or AI. Build manual planning and adaptation first.

## Final Agent Reminder

This product wins by being trustworthy.

That means:

- deterministic rules first;
- conservative safety defaults;
- transparent adaptation;
- Android as a clean client;
- AI as explanation only;
- Strava as optional read-only sync;
- small slices with tests.

If an implementation makes the system more magical but less explainable, reject it.
