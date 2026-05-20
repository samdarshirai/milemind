package com.company.runcoach.feature.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.runcoach.feature.onboarding.ui.components.CtaButtonGroup
import com.company.runcoach.feature.onboarding.ui.components.HeroBlock
import com.company.runcoach.feature.onboarding.ui.components.InfoBanner
import com.company.runcoach.feature.onboarding.ui.components.OnboardingScaffold
import com.company.runcoach.feature.onboarding.ui.components.SelectableChipGroup
import com.company.runcoach.feature.onboarding.ui.components.SectionCard
import com.company.runcoach.feature.onboarding.ui.components.StepProgressIndicator
import com.company.runcoach.feature.onboarding.ui.model.AvailabilityForm
import com.company.runcoach.feature.onboarding.ui.model.OnboardingEffect
import com.company.runcoach.feature.onboarding.ui.model.OnboardingStep
import com.company.runcoach.feature.onboarding.ui.model.RunningHistoryForm

private val sexOptions = listOf("FEMALE", "MALE", "OTHER", "PREFER_NOT_TO_SAY")
private val experienceOptions = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED")
private val weekdayOptions = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")

@Composable
fun OnboardingRoute(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OnboardingEffect.NavigateToRaceGoalPlaceholder -> onComplete()
                is OnboardingEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    when (state.step) {
        OnboardingStep.INTRO -> IntroScreen(
            onContinue = viewModel::nextFromIntro,
            snackbarHostState = snackbarHostState
        )
        OnboardingStep.RUNNING_HISTORY -> RunningHistoryScreen(
            form = state.runningHistoryForm,
            errors = state.fieldErrors,
            onChange = viewModel::updateRunningHistory,
            onNext = viewModel::nextFromRunningHistory,
            onBack = viewModel::back,
            snackbarHostState = snackbarHostState
        )
        OnboardingStep.AVAILABILITY -> AvailabilityScreen(
            form = state.availabilityForm,
            errors = state.fieldErrors,
            isLoading = state.isLoading,
            submitError = state.submitError,
            onChange = viewModel::updateAvailability,
            onSubmit = viewModel::submit,
            onBack = viewModel::back,
            snackbarHostState = snackbarHostState
        )
    }
}

@Composable
fun IntroScreen(onContinue: () -> Unit, snackbarHostState: SnackbarHostState? = null) {
    OnboardingScaffold(
        title = "Welcome to MILEMIND",
        subtitle = "Your plan uses deterministic coaching rules. AI explains decisions only.",
        snackbarHostState = snackbarHostState
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("intro_hero_block"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1115)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("intro_hero_image")
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(178.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFF2D323A), Color(0xFF090A0C))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(androidx.compose.ui.Alignment.BottomCenter)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFEF2CC1), Color(0xFFFC4C02), Color(0xFFC8F6F9))
                                )
                            )
                    )
                }
                Text("Let’s build your running profile", style = MaterialTheme.typography.headlineMedium, color = Color(0xFFE2E2E2))
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "We’ll ask about your recent running, weekly availability, and training preferences so your plan starts at a safe level.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF1B1B1B)
                )
                InfoBanner(
                    "Your plan is created by coaching rules. AI is used only to explain workouts and changes.",
                    tag = "intro_safety_banner"
                )
                CtaButtonGroup(modifier = Modifier.testTag("intro_cta_group")) {
                    Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().testTag("intro_primary_cta")) {
                        Text("Start setup")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        StepProgressIndicator(stepIndex = 0, steps = 3)
    }
}

@Composable
fun RunningHistoryScreen(
    form: RunningHistoryForm,
    errors: Map<String, String>,
    onChange: (RunningHistoryForm) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState? = null
) {
    OnboardingScaffold("Running history", "Help us set your baseline.", lightSection = true, snackbarHostState = snackbarHostState) {
        StepProgressIndicator(stepIndex = 1, steps = 3)
        HeroBlock(
            title = "Your recent running",
            subtitle = "This helps us avoid starting too hard.",
            tag = "running_history_hero_block"
        )
        SectionCard(title = "Training metrics", tag = "running_history_metrics_card") {
            OutlinedTextField(form.birthYear, { onChange(form.copy(birthYear = it)) }, label = { Text("Birth year") }, isError = errors["birthYear"] != null)
            errors["birthYear"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            OutlinedTextField(form.weeklyDistance, { onChange(form.copy(weeklyDistance = it)) }, label = { Text("Typical weekly distance (km)") }, isError = errors["weeklyDistance"] != null)
            errors["weeklyDistance"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            OutlinedTextField(form.longestRun, { onChange(form.copy(longestRun = it)) }, label = { Text("Longest recent run (km)") }, isError = errors["longestRun"] != null)
            errors["longestRun"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        SectionCard(title = "Profile details", tag = "running_history_profile_card") {
            Text("Sex", style = MaterialTheme.typography.titleSmall)
            sexOptions.forEach { sex ->
                Row(modifier = Modifier.padding(vertical = 2.dp).testTag("sex_option_$sex")) {
                    RadioButton(selected = form.sex == sex, onClick = { onChange(form.copy(sex = sex)) })
                    Text(sex.replace('_', ' '), modifier = Modifier.padding(top = 12.dp))
                }
            }
            errors["sex"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Text("Experience level", style = MaterialTheme.typography.titleSmall)
            experienceOptions.forEach { experience ->
                Row(modifier = Modifier.padding(vertical = 2.dp).testTag("experience_option_$experience")) {
                    RadioButton(selected = form.experienceLevel == experience, onClick = { onChange(form.copy(experienceLevel = experience)) })
                    Text(experience.replace('_', ' '), modifier = Modifier.padding(top = 12.dp))
                }
            }
            errors["experienceLevel"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        SectionCard(title = "Injury context", tag = "running_history_injury_card") {
            Text("Running injury in last 12 months?", style = MaterialTheme.typography.titleSmall)
            listOf("Yes" to true, "No" to false).forEach { (label, value) ->
                Row(modifier = Modifier.padding(vertical = 2.dp).testTag("injury_option_$label")) {
                    RadioButton(
                        selected = form.hadRunningInjuryLast12Months == value,
                        onClick = { onChange(form.copy(hadRunningInjuryLast12Months = value)) }
                    )
                    Text(label, modifier = Modifier.padding(top = 12.dp))
                }
            }
            errors["injuryHistory.hadRunningInjuryLast12Months"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            OutlinedTextField(form.injuryHistory, { onChange(form.copy(injuryHistory = it)) }, label = { Text("Injury history summary (optional)") })
            errors["injuryHistory.summary"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        CtaButtonGroup(modifier = Modifier.testTag("running_history_cta_group")) {
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth().testTag("running_history_primary_cta")) { Text("Next") }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().testTag("running_history_secondary_cta")) { Text("Back") }
        }
    }
}

@Composable
fun AvailabilityScreen(
    form: AvailabilityForm,
    errors: Map<String, String>,
    isLoading: Boolean,
    submitError: String?,
    onChange: (AvailabilityForm) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState? = null
) {
    OnboardingScaffold("Training availability", "Choose run days and units.", lightSection = true, snackbarHostState = snackbarHostState) {
        StepProgressIndicator(stepIndex = 2, steps = 3)
        HeroBlock(
            title = "When can you train?",
            subtitle = "Choose the days that are realistic most weeks.",
            tag = "availability_hero_block"
        )
        SectionCard(title = "Run schedule", tag = "availability_schedule_card") {
            Text("Preferred run days", style = MaterialTheme.typography.titleSmall)
            SelectableChipGroup(
                options = weekdayOptions,
                selected = form.preferredRunDays,
                onToggle = { day ->
                    onChange(
                        form.copy(
                            preferredRunDays = if (form.preferredRunDays.contains(day)) form.preferredRunDays - day else form.preferredRunDays + day
                        )
                    )
                }
            )
            errors["preferredRunDays"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Text("Preferred long run day", style = MaterialTheme.typography.titleSmall)
            weekdayOptions.forEach { day ->
                Row(modifier = Modifier.padding(vertical = 2.dp).testTag("long_run_option_$day")) {
                    RadioButton(selected = form.preferredLongRunDay == day, onClick = { onChange(form.copy(preferredLongRunDay = day)) })
                    Text(day, modifier = Modifier.padding(top = 12.dp))
                }
            }
            errors["preferredLongRunDay"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        SectionCard(title = "Strength and units", tag = "availability_preferences_card") {
            Text("Strength days per week", style = MaterialTheme.typography.titleSmall)
            Column {
                (0..2).forEach { value ->
                    Row(modifier = Modifier.padding(vertical = 2.dp).testTag("strength_option_$value")) {
                        RadioButton(selected = form.strengthDaysPerWeek == value, onClick = { onChange(form.copy(strengthDaysPerWeek = value)) })
                        Text(value.toString(), modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
            Text("Units", style = MaterialTheme.typography.titleSmall)
            Column {
                listOf("KM", "MILES").forEach { unit ->
                    Row(modifier = Modifier.padding(vertical = 2.dp).testTag("units_option_$unit")) {
                        RadioButton(selected = form.units == unit, onClick = { onChange(form.copy(units = unit)) })
                        Text(unit, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
            OutlinedTextField(
                value = form.timezone,
                onValueChange = { onChange(form.copy(timezone = it)) },
                label = { Text("Timezone") },
                isError = errors["timezone"] != null,
                modifier = Modifier.testTag("availability_timezone_input")
            )
            errors["timezone"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        submitError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (!submitError.isNullOrBlank()) {
            InfoBanner("Please review highlighted fields. Your profile has not been saved yet.")
        }
        CtaButtonGroup(modifier = Modifier.testTag("availability_cta_group")) {
            Button(onClick = onSubmit, enabled = !isLoading, modifier = Modifier.fillMaxWidth().testTag("availability_primary_cta")) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.padding(2.dp)) else Text("Save and continue")
            }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().testTag("availability_secondary_cta")) { Text("Back") }
        }
    }
}
