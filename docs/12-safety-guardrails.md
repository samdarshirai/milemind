# 12 Safety Guardrails

## Product Safety Principles

- Conservative defaults beat aggressive optimization.
- The product manages training risk. It does not diagnose health conditions.
- Missing data should reduce confidence and increase conservatism.
- One strong workout never justifies a sudden increase.
- Pain and illness should remove intensity faster than fitness ambition would prefer.

## Injury And Pain Handling

- High pain always overrides performance goals.
- Sharp localized pain is treated as higher risk than general soreness.
- Pain during a run is treated as higher risk than normal next-day fatigue.
- If the user cannot continue running, the app must remove intensity immediately.
- The app must never tell users to push through severe pain.

## Medical Disclaimer Boundaries

Allowed:
- "If pain is severe, worsening, or affecting normal movement, consider seeking evaluation from a qualified professional."
- "This app cannot diagnose injuries."

Prohibited:
- "You have a stress reaction."
- "This is definitely tendonitis."
- "It is safe to keep racing."

## Overtraining Prevention

- Weekly load increases capped by deterministic policy.
- Long-run increases capped separately.
- Recovery week inserted every fourth week by default.
- Readiness orange or red removes or reduces quality.
- Missed workout flow never instructs catch-up stacking.

## Conservative Defaults

Recommended MVP default:
- Choose easier branch when multiple safe options exist.
- Choose duration-based easy running instead of pace pressure when fitness certainty is low.
- Reject race-goal dates that force unsafe progression instead of compressing training aggressively.

## AI Safety Rules

- AI may explain only approved plan state.
- AI may not generate a plan.
- AI may not advise beyond deterministic guardrails.
- AI outputs must be schema-validated and policy-filtered.
- AI confidence failures fall back to deterministic text.

## User-Facing Safety Copy Suggestions

### High Pain Banner

"Your training has been reduced because you reported high pain. Focus on recovery first. This app cannot diagnose injuries."

### Illness Banner

"Because you reported illness, this week has been simplified to protect recovery. Do not try to make up missed hard sessions."

### Overdone Workout Banner

"You did more than planned. The next sessions have been softened to protect consistency."

### AI Boundary Copy

"Coach explanations are generated from your existing plan and training data. They do not create or replace your training plan."

### Medical Boundary Copy

"This app can help you train more conservatively, but it cannot diagnose medical issues or replace professional care."

## Safety Triggers Requiring Immediate Downshift

- Pain severity 7 or higher
- Sharp localized pain during run
- Cannot continue running
- Illness flag with moderate or severe symptoms
- Repeated under-completion of key sessions with rising fatigue

## Safety Triggers Requiring Manual Review Or Support Attention

- Repeated red readiness states across 2 weeks
- Multiple unmatched overdone long runs
- Frequent severe pain reports
- Pattern of repeated reschedule plus skip loops suggesting unsustainable goal

## Things The Product Must Not Say

- "Push through it."
- "Ignore the pain."
- "You are definitely injured."
- "You can make up your missed long run by doubling this weekend."
- "This new plan guarantees your goal time."
