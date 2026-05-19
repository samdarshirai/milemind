# 11 DeepSeek AI Integration

## Usage Policy

DeepSeek is used only for:
- Workout explanations
- Adaptation explanations
- Weekly summaries
- Safe coaching chat limited to explanation and support

DeepSeek is not used for:
- Plan generation
- Plan adaptation decisioning
- Injury diagnosis
- Medical advice
- Performance guarantees

## Backend-Only API Calls

Rules:
- Android never calls DeepSeek directly.
- Backend builds prompts from deterministic state only.
- Backend validates every response against schema and safety rules before persistence or display.

## Model Selection Recommendation

Recommended MVP default:
- Use a lower-latency general-purpose DeepSeek chat model for workout and adaptation explanations.
- Use the same model for chat initially to reduce operational complexity.
- Do not introduce model routing in MVP unless latency or cost data proves it necessary.

Implementation note:
- Hide exact model ID behind configuration property `ai.deepseek.model`.
- Keep prompt and validator contract stable even if model version changes.

## Prompt Architecture

Every prompt must contain:
- System policy
- Structured runner context
- Structured plan or adaptation facts
- Explicit task
- JSON-only output contract

## Prompt Template: Explain Workout

```json
{
  "task": "explain_workout",
  "runnerContext": {
    "goalRace": "HALF_MARATHON",
    "experienceLevel": "BEGINNER",
    "readinessState": "GREEN"
  },
  "workoutContext": {
    "workoutType": "THRESHOLD",
    "workoutSubtype": "CRUISE_INTERVALS",
    "phase": "BUILD",
    "purposeCodes": [
      "THRESHOLD_DEVELOPMENT",
      "SAFE_INTENSITY_DISTRIBUTION"
    ]
  },
  "outputRequirements": {
    "maxWords": 100,
    "tone": "calm_clear_non_medical",
    "mustInclude": [
      "what the workout does",
      "how it should feel"
    ]
  }
}
```

## Prompt Template: Explain Adaptation

```json
{
  "task": "explain_adaptation",
  "runnerContext": {
    "goalRace": "MARATHON",
    "weeksToRace": 7,
    "fatigueState": "ORANGE",
    "painState": "MILD_DIFFUSE"
  },
  "decisionContext": {
    "decisionType": "DOWNSHIFT_WEEK",
    "reasonCodes": [
      "HIGH_FATIGUE_SCORE",
      "LOW_COMPLETION_RECENT_KEY_WORKOUTS",
      "PRESERVE_LONG_RUN"
    ],
    "changes": [
      {
        "from": "VO2",
        "to": "EASY"
      }
    ]
  },
  "outputRequirements": {
    "maxWords": 120,
    "tone": "calm_clear_non_medical",
    "mustInclude": [
      "what changed",
      "why it changed",
      "what to focus on now"
    ]
  }
}
```

## Prompt Template: Safe Coaching Chat

```json
{
  "task": "safe_coaching_chat",
  "allowedIntent": "EXPLANATION_ONLY",
  "userQuestion": "Why did my threshold run change?",
  "runnerContext": {
    "readinessState": "ORANGE"
  },
  "guardrails": {
    "cannotCreatePlan": true,
    "cannotGiveMedicalAdvice": true,
    "mustReferenceReasonCodes": true
  }
}
```

## JSON Response Schemas

### Workout Explanation Schema

```json
{
  "messageType": "WORKOUT_EXPLANATION",
  "headline": "This workout builds sustainable speed.",
  "summary": "The intervals help you run faster without turning the session into an all-out effort.",
  "actionItems": [
    "Keep the reps controlled.",
    "You should finish feeling worked, not emptied."
  ],
  "confidence": 0.92,
  "safetyFlags": [],
  "reasonCodes": [
    "THRESHOLD_DEVELOPMENT",
    "SAFE_INTENSITY_DISTRIBUTION"
  ]
}
```

### Adaptation Explanation Schema

```json
{
  "messageType": "ADAPTATION_EXPLANATION",
  "headline": "Your week has been dialed back to protect recovery.",
  "summary": "We reduced intensity because your fatigue signals are elevated and recent key workouts were under-completed.",
  "actionItems": [
    "Keep the next run easy.",
    "Recheck soreness after the next two runs."
  ],
  "confidence": 0.91,
  "safetyFlags": [],
  "reasonCodes": [
    "HIGH_FATIGUE_SCORE",
    "LOW_COMPLETION_RECENT_KEY_WORKOUTS"
  ]
}
```

### Safe Chat Schema

```json
{
  "messageType": "SAFE_EXPLANATION",
  "reply": "Your threshold run changed because the engine is protecting recovery before your long run.",
  "confidence": 0.87,
  "reasonCodes": [
    "HIGH_FATIGUE_SCORE",
    "PROTECT_LONG_RUN"
  ],
  "safetyFlags": []
}
```

## Validation Rules

Reject output if:
- Not valid JSON
- Missing required fields
- Confidence below configured minimum
- Contradicts deterministic decision
- Suggests load increase above safety caps
- Contains diagnosis or medical claims
- Omits required reason codes
- Gives advice outside allowed chat scope

## Fallback Behavior

If validation fails:
- Log rejection reason
- Return deterministic template text
- Persist source as `DETERMINISTIC_FALLBACK`

Fallback example:
- "Your next week was reduced because fatigue is elevated and recent key workouts were not completed as planned. Focus on easy running and reassess after two runs."

## Prohibited AI Behavior

- Creating or editing training plans
- Overriding training engine decisions
- Diagnosing injury or illness
- Advising a user to ignore pain
- Claiming certainty not supported by data
- Using Strava data for model training
- Producing free-form dangerous encouragement such as pushing through severe pain

## Safety Guardrails

- Use calm, non-medical language.
- If pain severity is high, point user to conservative next steps and professional-care guidance copy.
- If the user asks for a new plan in chat, redirect to structured goal and profile flows.
- If the user asks for diagnosis, refuse and display medical boundary copy.

## Logging And Audit

- Store prompt template version.
- Store model ID.
- Store validator result.
- Store displayed response source: `AI_VALIDATED` or `DETERMINISTIC_FALLBACK`.
- Redact personally sensitive free-text fields where required.

## Recommended MVP Defaults

- One DeepSeek model config for all explanation use cases.
- One validator pipeline shared across explanation and chat.
- One conservative confidence threshold for display.
