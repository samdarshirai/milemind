package com.company.runcoach.feature.onboarding.ui.model

enum class OnboardingStep { INTRO, RUNNING_HISTORY, AVAILABILITY }

data class RunningHistoryForm(
    val birthYear: String = "",
    val sex: String? = null,
    val experienceLevel: String? = null,
    val weeklyDistance: String = "",
    val longestRun: String = "",
    val hadRunningInjuryLast12Months: Boolean? = null,
    val injuryHistory: String = ""
)

data class AvailabilityForm(
    val preferredRunDays: Set<String> = setOf(),
    val preferredLongRunDay: String? = null,
    val strengthDaysPerWeek: Int = 1,
    val units: String = "KM",
    val timezone: String = java.time.ZoneId.systemDefault().id
)

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.INTRO,
    val runningHistoryForm: RunningHistoryForm = RunningHistoryForm(),
    val availabilityForm: AvailabilityForm = AvailabilityForm(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val submitError: String? = null,
    val isSuccess: Boolean = false
)

sealed interface OnboardingEffect {
    data object NavigateToRaceGoalPlaceholder : OnboardingEffect
    data class ShowMessage(val message: String) : OnboardingEffect
}
