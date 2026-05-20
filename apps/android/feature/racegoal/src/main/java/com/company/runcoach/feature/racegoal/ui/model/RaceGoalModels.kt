package com.company.runcoach.feature.racegoal.ui.model

import com.company.runcoach.feature.racegoal.data.RaceGoal

enum class RaceGoalStep {
    SETUP,
    REVIEW,
    SAVED
}

data class RaceGoalForm(
    val raceDistanceType: String? = null,
    val raceName: String = "",
    val raceDate: String = "",
    val goalStyle: String? = null,
    val targetTime: String = ""
)

data class RaceGoalUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val step: RaceGoalStep = RaceGoalStep.SETUP,
    val form: RaceGoalForm = RaceGoalForm(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val submitError: String? = null,
    val tooSoonMessage: String? = null,
    val activeGoalMessage: String? = null,
    val currentGoal: RaceGoal? = null
)

sealed interface RaceGoalEffect {
    data object NavigateToPlanPlaceholder : RaceGoalEffect
    data class ShowMessage(val message: String) : RaceGoalEffect
}
