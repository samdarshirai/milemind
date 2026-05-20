package com.company.runcoach.feature.racegoal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.runcoach.feature.onboarding.ui.components.InfoBanner
import com.company.runcoach.feature.onboarding.ui.components.OnboardingScaffold
import com.company.runcoach.feature.onboarding.ui.components.StepProgressIndicator
import com.company.runcoach.feature.racegoal.ui.components.GoalStyleSelector
import com.company.runcoach.feature.racegoal.ui.components.RaceDistanceCard
import com.company.runcoach.feature.racegoal.ui.components.RaceGoalSummaryCard
import com.company.runcoach.feature.racegoal.ui.components.TargetTimeInput
import com.company.runcoach.feature.racegoal.ui.model.RaceGoalEffect
import com.company.runcoach.feature.racegoal.ui.model.RaceGoalForm
import com.company.runcoach.feature.racegoal.ui.model.RaceGoalStep

@Composable
fun RaceGoalRoute(onComplete: () -> Unit, viewModel: RaceGoalViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RaceGoalEffect.NavigateToPlanPlaceholder -> onComplete()
                is RaceGoalEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (state.currentGoal != null && state.step == RaceGoalStep.SETUP) {
        ExistingGoalScreen(onContinue = viewModel::continueWithExistingGoal)
        return
    }

    when (state.step) {
        RaceGoalStep.SETUP -> RaceGoalSetupScreen(
            form = state.form,
            errors = state.fieldErrors,
            tooSoonMessage = state.tooSoonMessage,
            activeGoalMessage = state.activeGoalMessage,
            submitError = state.submitError,
            onFormChange = viewModel::updateForm,
            onContinue = viewModel::continueToReview,
            snackbarHostState = snackbarHostState
        )
        RaceGoalStep.REVIEW -> RaceGoalReviewScreen(
            form = state.form,
            isSaving = state.isSaving,
            onBack = viewModel::backToSetup,
            onConfirm = viewModel::saveGoal,
            snackbarHostState = snackbarHostState
        )
        RaceGoalStep.SAVED -> GoalSavedScreen(onContinue = viewModel::continueAfterSaved)
    }
}

@Composable
fun RaceGoalSetupScreen(
    form: RaceGoalForm,
    errors: Map<String, String>,
    tooSoonMessage: String?,
    activeGoalMessage: String?,
    submitError: String?,
    onFormChange: (RaceGoalForm) -> Unit,
    onContinue: () -> Unit,
    snackbarHostState: SnackbarHostState? = null
) {
    OnboardingScaffold("What are you training for?", "Choose one race goal so your plan can stay focused.", lightSection = true, snackbarHostState = snackbarHostState) {
        StepProgressIndicator(stepIndex = 0, steps = 2)
        RaceDistanceCard(selected = form.raceDistanceType, onSelect = { onFormChange(form.copy(raceDistanceType = it)) })
        Text("RACE NAME (OPTIONAL)", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color(0xFF4C4546))
        androidx.compose.material3.OutlinedTextField(
            value = form.raceName,
            onValueChange = { onFormChange(form.copy(raceName = it)) },
            placeholder = { Text("e.g. Berlin Marathon") },
            modifier = Modifier.fillMaxWidth().testTag("race_name_input")
        )
        Text("RACE DATE", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color(0xFF4C4546))
        androidx.compose.material3.OutlinedTextField(
            value = form.raceDate,
            onValueChange = { onFormChange(form.copy(raceDate = it)) },
            placeholder = { Text("YYYY-MM-DD") },
            isError = errors["raceDate"] != null,
            modifier = Modifier.fillMaxWidth().testTag("race_date_input")
        )
        errors["raceDate"]?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }

        GoalStyleSelector(selected = form.goalStyle, onSelect = { onFormChange(form.copy(goalStyle = it)) })
        TargetTimeInput(
            value = form.targetTime,
            error = errors["targetTimeSeconds"],
            onValueChange = { onFormChange(form.copy(targetTime = it)) }
        )
        errors["raceDistanceType"]?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
        errors["goalStyle"]?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
        tooSoonMessage?.let { InfoBanner(it, tag = "race_date_too_soon_banner") }
        activeGoalMessage?.let { InfoBanner(it, tag = "existing_active_goal_banner") }
        submitError?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().testTag("race_goal_continue")) { Text("Review goal") }
    }
}

@Composable
fun RaceGoalReviewScreen(
    form: RaceGoalForm,
    isSaving: Boolean,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    snackbarHostState: SnackbarHostState? = null
) {
    OnboardingScaffold("Review race goal", "Confirm these details before saving.", lightSection = true, snackbarHostState = snackbarHostState) {
        StepProgressIndicator(stepIndex = 1, steps = 2)
        RaceGoalSummaryCard(form = form)
        Button(onClick = onConfirm, enabled = !isSaving, modifier = Modifier.fillMaxWidth().testTag("race_goal_confirm")) {
            if (isSaving) CircularProgressIndicator() else Text("Save goal")
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().testTag("race_goal_back")) { Text("Back") }
    }
}

@Composable
fun GoalSavedScreen(onContinue: () -> Unit) {
    OnboardingScaffold("Goal saved", "Your race goal is active. Next we will generate your plan.", lightSection = true) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoBanner("Plan generation is coming in the next slice.", tag = "goal_saved_banner")
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().testTag("goal_saved_continue")) { Text("Continue") }
        }
    }
}

@Composable
internal fun ExistingGoalScreen(onContinue: () -> Unit) {
    OnboardingScaffold("Race goal active", "You already have an active race goal.", lightSection = true) {
        InfoBanner("Keep training with your current goal. This app cannot guarantee race outcomes.", tag = "current_goal_summary")
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().testTag("existing_goal_continue")) { Text("Continue") }
    }
}
