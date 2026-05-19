# 16 Screen Design Prompts

## Purpose

This file defines the screen design prompts and design handoff instructions for the native Android adaptive running coach app.

Use this file when creating screen designs in Stitch, Figma AI, Lovable, or another UI design tool.

This file is not an implementation spec. It is a design-generation and UX-handoff document. Implementation details live in:

- `03-android-app-spec.md`
- `04-screen-navigation.md`
- `07-api-contracts.md`
- `13-implementation-slices.md`

## Product Context For Designers

The app is a native Android adaptive running coach for adult road runners training for half marathon and marathon goals.

The product is not a generic fitness tracker and not a free-form AI coach. It is a structured training app where:

- the backend creates deterministic training plans;
- the backend adapts plans using explicit rules;
- the Android app displays the plan and collects user input;
- AI explains approved plan and adaptation decisions;
- safety guardrails override performance ambition.

The design must make the app feel trustworthy, calm, and coach-like.

## Core Design Principles

### 1. Today's action must be obvious

The user should immediately understand:

- whether they run today;
- what kind of workout it is;
- how hard it should feel;
- why the workout exists;
- whether anything changed recently.

### 2. Adaptation must be visible

If the plan changes, do not hide it.

Use clear UI markers such as:

- `What changed?`
- `Adjusted for recovery`
- `Intensity reduced`
- `Long run protected`
- `Plan updated`

Every visible adaptation should have a path to a plain-language explanation.

### 3. Safety states must be clear but not scary

Pain, illness, and fatigue states should be visible, but the tone must remain calm.

Avoid panic language.

Good:
- "Training has been reduced to protect recovery."
- "Keep the next run easy and reassess after two sessions."

Bad:
- "Warning: injury risk detected."
- "You may be injured."

### 4. The app should not feel like a chatbot

The Coach tab is supportive, but the app's center of gravity is the training plan.

Do not design the app around a big chat input on the home screen.

### 5. Progressive disclosure for running concepts

Beginners should not face jargon-heavy screens.

Terms like threshold, taper, recovery week, strides, and RPE should be explainable through small inline help affordances.

### 6. Calm premium aesthetic

Use a serious, modern, clean Android look.

Avoid:

- neon-heavy fitness app visuals;
- aggressive streak gamification;
- social-feed patterns;
- cluttered analytics dashboards;
- medical-app fear language.

## Platform Direction

The app is native Android.

Design should assume:

- Kotlin
- Jetpack Compose
- Material 3
- bottom navigation
- card-based layouts
- accessible typography
- light and dark mode readiness
- standard Android gestures and system back behavior

Designs should be practical to implement in Compose.

## Primary Navigation

Primary bottom tabs:

1. Today
2. Plan
3. Progress
4. Coach
5. Profile

The primary navigation should stay stable after onboarding.

## Global UI Components

Design these reusable components early:

### Workout Card

Used on Today, Plan, Progress, and Workout Detail.

Should show:

- workout type;
- headline;
- scheduled date;
- duration or distance;
- intensity cue;
- status;
- adaptation marker if changed.

### Readiness Card

Used on Today and Progress.

Should show:

- readiness state: Green, Yellow, Orange, Red;
- short explanation;
- CTA to complete check-in;
- safety banner when needed.

### Adaptation Notice

Used on Today, Plan, Workout Detail, and Coach.

Should show:

- headline;
- reason chips;
- changed workout summary;
- CTA: `View what changed`.

### Safety Banner

Used when pain, illness, or red readiness exists.

Should show:

- short conservative copy;
- no medical diagnosis;
- reduced CTA set.

### Empty State

Needed for:

- no plan yet;
- rest day;
- no progress data;
- no Strava activities;
- no Coach history.

### Error State

Needed for:

- network failure;
- auth expired;
- stale plan version;
- Strava connection problem;
- AI explanation unavailable.

### Loading State

Needed for:

- plan generation;
- screen-level data loading;
- Strava sync;
- AI explanation generation.

## Copy Style

Use copy that is:

- calm;
- direct;
- non-medical;
- supportive without hype;
- plain-language first;
- precise enough for serious runners.

Avoid:

- "crush it";
- "no pain, no gain";
- "push through";
- "guaranteed result";
- "AI has decided";
- "injury detected".

Preferred examples:

- "Easy run. Keep it conversational."
- "This week was adjusted to protect recovery."
- "You reported high pain, so faster running has been removed for now."
- "This app cannot diagnose injuries."

## Design Batch 1: Auth And Onboarding

### Related Docs

Use:

- `01-product-summary.md`
- `02-mvp-scope.md`
- `03-android-app-spec.md`
- `04-screen-navigation.md`
- `07-api-contracts.md`
- `12-safety-guardrails.md`
- `13-implementation-slices.md`, Slices 1 to 3

### Screens

- Splash
- Sign In
- Sign Up
- Onboarding Intro
- Onboarding Running History
- Onboarding Availability
- Onboarding Race Goal
- Onboarding Review
- Plan Generation Loading
- Plan Generated Summary

### Design Prompt

Design a native Android onboarding flow for an adaptive running coach app.

The app helps adult road runners train for half marathon and marathon goals using deterministic backend training rules, conservative adaptation, optional Strava sync, and AI-generated explanations.

Create screen designs for:

1. Splash
2. Sign In
3. Sign Up
4. Onboarding Intro
5. Running History
6. Training Availability
7. Race Goal
8. Onboarding Review
9. Plan Generation Loading
10. Plan Generated Summary

Style requirements:

- Native Android / Material 3 inspired
- Calm, premium, trustworthy
- Clean cards, readable typography, generous spacing
- No neon-heavy fitness aesthetic
- No social-feed style
- No chatbot-first layout
- Light and dark mode friendly

UX requirements:

- Onboarding should feel like a coach intake, not a survey dump.
- Show step progress.
- Keep forms focused and grouped.
- Use helper text for running-specific inputs.
- Make the safety positioning clear.
- Explain that AI explains decisions but does not create the plan.
- The review screen must show user assumptions before plan generation.

Required input areas:

- birth year;
- experience level;
- current weekly running distance;
- longest recent run;
- available run days;
- preferred long-run day;
- strength availability;
- half marathon or marathon;
- race date;
- goal style;
- optional target time;
- injury history summary.

Validation behavior to represent:

- invalid email;
- weak password;
- race date too soon;
- too few available run days;
- missing longest recent run;
- under-18 user blocked.

Important copy examples:

- "Your plan is built from coaching rules, not generated freely by AI."
- "You can adjust your plan later when life gets in the way."
- "This app cannot diagnose injuries, but it will adapt conservatively when you report pain."

Output expected:

- screen-by-screen visual design;
- component notes;
- CTA labels;
- loading, error, and success states;
- notes for Jetpack Compose implementation.

## Design Batch 2: Today And Plan

### Related Docs

Use:

- `04-screen-navigation.md`
- `07-api-contracts.md`
- `08-training-engine-rules.md`
- `13-implementation-slices.md`, Slice 4

### Screens

- Today
- Rest Day Today
- Plan Calendar
- Week View
- Workout Detail
- Recovery Week State
- Workout Changed State

### Design Prompt

Design the Today and Plan experience for a native Android adaptive running coach app.

The user's main job is to understand what to do today and how the current training week fits into their race plan.

Create screen designs for:

1. Today with planned workout
2. Today rest-day state
3. Today with safety banner
4. Plan Calendar week view
5. Plan Calendar day view
6. Workout Detail
7. Recovery Week state
8. Workout changed state

Style requirements:

- Material 3 inspired
- Calm, structured, premium
- Strong information hierarchy
- Clear workout cards
- Easy-to-scan week layout
- Avoid cluttered spreadsheet-like planning UI

Today screen must show:

- today's workout or rest day;
- workout type;
- duration or distance;
- effort/intensity cue;
- why this workout exists;
- readiness state;
- quick action to complete check-in;
- any latest adaptation summary.

Plan screen must show:

- current week;
- phase label: Base, Build, Race Specific, Taper;
- recovery week label when relevant;
- day-by-day workouts;
- status: planned, completed, skipped, rescheduled, replaced;
- `What changed?` marker on adapted workouts.

Workout Detail must show:

- workout headline;
- structure: warm-up, main set, cool-down;
- pace or effort cue;
- rationale;
- safety notices;
- actions: Complete, Skip, Reschedule, Ask Coach.

States to include:

- loading;
- empty plan;
- rest day;
- network error;
- stale plan conflict;
- AI explanation unavailable fallback.

Important copy examples:

- "45 min easy run"
- "Keep it conversational. You should finish fresher than you started."
- "Recovery week: volume is lower so your body can absorb training."
- "This workout changed because recent fatigue was elevated."

Output expected:

- mobile screen designs;
- reusable component notes;
- CTA labels;
- empty, loading, error, success states;
- Compose implementation notes.

## Design Batch 3: Manual Completion And Check-Ins

### Related Docs

Use:

- `04-screen-navigation.md`
- `07-api-contracts.md`
- `09-adaptation-engine-rules.md`
- `12-safety-guardrails.md`
- `13-implementation-slices.md`, Slices 5 and 6

### Screens

- Complete Workout
- Completion Success
- Fatigue Check-In
- Pain Check-In
- Readiness Result
- High Pain Safety Result

### Design Prompt

Design the workout completion and check-in flows for a native Android adaptive running coach app.

These flows must be fast, low-friction, and safe. A user should be able to complete post-workout feedback in under 20 seconds.

Create screen designs for:

1. Complete Workout
2. Completion Success
3. Fatigue Check-In
4. Pain Check-In
5. Readiness Result
6. High Pain Safety Result

Style requirements:

- calm;
- mobile-first;
- accessible touch targets;
- simple sliders or segmented controls;
- minimal typing;
- clear success feedback;
- non-medical pain language.

Complete Workout must collect:

- distance;
- duration;
- RPE;
- felt easier / on target / harder;
- optional notes.

Fatigue Check-In must collect:

- sleep;
- stress;
- soreness;
- motivation;
- illness flag;
- travelling flag;
- too busy flag.

Pain Check-In must collect:

- body region;
- severity 0 to 10;
- sharp or diffuse;
- during run or after run;
- can continue running;
- optional note.

Safety behavior to show:

- severity 7 or higher triggers reduced training state;
- sharp localized pain is treated conservatively;
- cannot continue running triggers red readiness state;
- app must not diagnose injury.

Important copy examples:

- "How did it feel?"
- "This helps the plan adapt safely."
- "You reported high pain, so faster running has been removed for now."
- "This app cannot diagnose injuries. Consider professional care if pain is severe, worsening, or affecting normal movement."

Output expected:

- screen designs;
- input components;
- validation states;
- safety copy;
- submission error states;
- Compose implementation notes.

## Design Batch 4: Adaptation

### Related Docs

Use:

- `04-screen-navigation.md`
- `08-training-engine-rules.md`
- `09-adaptation-engine-rules.md`
- `12-safety-guardrails.md`
- `13-implementation-slices.md`, Slice 7

### Screens

- Adaptation Summary Card
- What Changed Detail
- Changed Workout Detail
- Skip Workout Flow
- Reschedule Workout Flow
- Stale Plan Conflict
- Recovery Week Conversion

### Design Prompt

Design the adaptation experience for a native Android adaptive running coach app.

The app changes the near-term training plan after missed workouts, partial completions, overdone workouts, fatigue, illness, or pain. The user must clearly understand what changed and why.

Create screen designs for:

1. Adaptation Summary Card on Today
2. What Changed Detail
3. Changed Workout Detail
4. Skip Workout Flow
5. Reschedule Workout Flow
6. Stale Plan Conflict
7. Recovery Week Conversion

Style requirements:

- transparent;
- calm;
- no alarmist visuals;
- reason chips;
- clear before/after comparison;
- concise copy;
- no hidden plan changes.

Adaptation Summary must show:

- adaptation headline;
- primary reason codes translated to readable chips;
- affected dates;
- CTA to view details.

What Changed Detail must show:

- changed workouts;
- before and after values;
- reason explanation;
- safety note where applicable.

Skip Workout Flow must show:

- missed easy run: no make-up volume;
- missed quality run: reschedule only if safe;
- missed long run: conservative options only.

Reschedule Flow must show:

- allowed dates;
- spacing warnings;
- blocked unsafe moves.

Stale Plan Conflict must show:

- "Your plan changed since this screen loaded."
- CTA: "Refresh plan"

Important reason chip examples:

- "High fatigue"
- "Pain reported"
- "Long run protected"
- "No catch-up stacking"
- "Intensity reduced"

Important copy examples:

- "We removed the faster workout and kept the easy run."
- "You do not need to make up the missed session."
- "This move is blocked because it would place two hard sessions too close together."

Output expected:

- screen designs;
- component notes;
- before/after comparison pattern;
- reason chip pattern;
- Compose implementation notes.

## Design Batch 5: Progress And Insights

### Related Docs

Use:

- `04-screen-navigation.md`
- `07-api-contracts.md`
- `13-implementation-slices.md`, Slice 8

### Screens

- Progress Dashboard
- New User Progress Empty State
- Long-Run Progression
- Weekly Completion Trend
- Readiness Trend
- Recent Adaptations

### Design Prompt

Design the Progress screen for a native Android adaptive running coach app.

The Progress screen should motivate consistency without encouraging reckless mileage chasing.

Create screen designs for:

1. Progress Dashboard
2. New User Progress Empty State
3. Long-Run Progression
4. Weekly Completion Trend
5. Readiness Trend
6. Recent Adaptations

Style requirements:

- clean analytics;
- calm visual hierarchy;
- no aggressive streak pressure;
- simple chart cards;
- readable summaries;
- beginner-friendly explanations.

Progress screen must show:

- weekly completion rate;
- weekly distance trend;
- long-run progression;
- readiness trend;
- recent adaptation count;
- explanation of what the metrics mean.

Avoid:

- leaderboards;
- calorie focus;
- weight-loss framing;
- guilt-based streak language;
- complex TrainingPeaks-style dashboards in MVP.

Important copy examples:

- "Consistency is the goal this week."
- "Your long run is progressing gradually."
- "Recent adaptations helped protect recovery."

Output expected:

- dashboard design;
- chart-card design;
- empty state;
- error state;
- Compose implementation notes.

## Design Batch 6: Strava

### Related Docs

Use:

- `04-screen-navigation.md`
- `07-api-contracts.md`
- `10-strava-integration.md`
- `13-implementation-slices.md`, Slices 9 to 11

### Screens

- Strava Connection
- Strava Connect Explanation
- OAuth Return Success
- OAuth Return Failure
- Strava Status
- Import History
- Import Review
- Disconnect Confirmation

### Design Prompt

Design the Strava integration screens for a native Android adaptive running coach app.

Strava is optional and read-only in MVP. The app must make this clear.

Create screen designs for:

1. Strava Connection
2. Strava Connect Explanation
3. OAuth Return Success
4. OAuth Return Failure
5. Strava Status
6. Import History
7. Import Review
8. Disconnect Confirmation

Style requirements:

- trustworthy;
- privacy-aware;
- clear permission explanation;
- simple status indicators;
- no overpromising.

Strava connection screen must show:

- connection status;
- last sync time;
- granted scope summary;
- CTA to connect or disconnect;
- explanation that Strava reduces manual logging but is optional.

Import Review must show:

- imported activity;
- candidate planned workout;
- match confidence;
- confirm match CTA;
- leave unmatched CTA.

Important copy examples:

- "Strava sync is optional. You can still complete workouts manually."
- "MVP Strava integration is read-only."
- "We import your activities to match completed runs to your plan."
- "This activity needs review before it is linked."

Output expected:

- connection flow designs;
- status states;
- import review pattern;
- error states;
- Compose implementation notes.

## Design Batch 7: Coach And AI Explanations

### Related Docs

Use:

- `04-screen-navigation.md`
- `07-api-contracts.md`
- `11-deepseek-ai-integration.md`
- `12-safety-guardrails.md`
- `13-implementation-slices.md`, Slice 12

### Screens

- Coach Home
- Suggested Prompts
- Workout Explanation
- Adaptation Explanation
- Safe Chat
- Chat History
- AI Fallback State
- Medical Boundary State

### Design Prompt

Design the Coach tab and AI explanation experience for a native Android adaptive running coach app.

AI is used only to explain existing plan and adaptation decisions. AI must not appear to create or edit the plan.

Create screen designs for:

1. Coach Home
2. Suggested Prompts
3. Workout Explanation
4. Adaptation Explanation
5. Safe Chat
6. Chat History
7. AI Fallback State
8. Medical Boundary State

Style requirements:

- supportive;
- controlled;
- explanation-first;
- not chatbot-dominant;
- transparent about limitations.

Coach Home must show:

- suggested safe prompts;
- recent explanations;
- latest adaptation explanation;
- boundary note.

Suggested prompts:

- "Why did my week change?"
- "What should today's easy run feel like?"
- "What does threshold mean?"
- "Why is this a recovery week?"
- "Can I move this easy run?"

Blocked or redirected prompts:

- creating a new plan;
- diagnosing injuries;
- telling user to ignore pain;
- increasing workload beyond plan.

AI fallback state must show deterministic fallback copy when AI is unavailable or rejected.

Important copy examples:

- "Coach explanations are generated from your existing plan. They do not create or replace your plan."
- "I cannot diagnose injuries. If pain is severe or worsening, consider professional care."
- "Your plan was changed by the training engine. This explanation summarizes why."

Output expected:

- screen designs;
- suggested prompt cards;
- chat bubble pattern;
- fallback pattern;
- boundary states;
- Compose implementation notes.

## Design Batch 8: Profile And Settings

### Related Docs

Use:

- `04-screen-navigation.md`
- `07-api-contracts.md`
- `10-strava-integration.md`
- `12-safety-guardrails.md`

### Screens

- Profile
- Edit Profile
- Goal Summary
- Preferences
- Units And Timezone
- Safety And Disclaimer
- Data And Privacy
- Strava Entry Point
- Sign Out

### Design Prompt

Design the Profile and Settings screens for a native Android adaptive running coach app.

Create screen designs for:

1. Profile
2. Edit Profile
3. Goal Summary
4. Preferences
5. Units and Timezone
6. Safety and Disclaimer
7. Data and Privacy
8. Strava Entry Point
9. Sign Out

Style requirements:

- clean settings layout;
- clear sections;
- privacy-aware;
- no unnecessary complexity.

Profile must show:

- user account summary;
- runner profile summary;
- current race goal;
- training preferences;
- Strava connection status;
- safety disclaimer access;
- sign out.

Important copy examples:

- "Training logic runs on the backend so your plan stays consistent across devices."
- "You can disconnect Strava at any time."
- "This app cannot diagnose medical issues or replace professional care."

Output expected:

- settings screen designs;
- edit profile flow;
- privacy/safety copy placement;
- Compose implementation notes.

## Design Handoff Requirements

Every generated design batch should produce:

- screen list;
- visual screens;
- main components;
- CTA labels;
- loading states;
- empty states;
- error states;
- success states;
- safety states where relevant;
- backend data needed per screen;
- implementation notes for Jetpack Compose.

## Design Review Checklist

Before implementation, verify:

- Screens match `04-screen-navigation.md`.
- No screen requires backend data not available in `07-api-contracts.md`.
- No screen implies Android owns training logic.
- No screen implies AI creates or edits plans.
- Safety copy follows `12-safety-guardrails.md`.
- Strava screens clearly say read-only and optional.
- Adaptation states are visible and explainable.
- Forms are short enough for mobile use.
- Loading, empty, error, and success states exist.
- Designs are realistic to implement in Jetpack Compose.

## Common Design Mistakes To Avoid

- Making the Coach tab the main product.
- Hiding adaptation behind generic notifications.
- Using guilt-based streaks.
- Overloading the Progress screen with advanced metrics.
- Showing unsafe "make up missed workout" CTAs.
- Treating pain as a normal training inconvenience.
- Using medical diagnosis language.
- Designing smartwatch or wearable flows in MVP.
- Designing payment screens in MVP.
- Designing social/community features in MVP.

## Recommended Design Sequence

Design one batch ahead of implementation.

Recommended order:

1. Auth and Onboarding
2. Today and Plan
3. Completion and Check-Ins
4. Adaptation
5. Progress
6. Strava
7. Coach
8. Profile and Settings

Do not design the entire future product before the MVP flows are implemented.
