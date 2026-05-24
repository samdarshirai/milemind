package com.company.runcoach.feature.checkin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.runcoach.feature.checkin.ui.model.FatigueFormState
import com.company.runcoach.feature.checkin.ui.model.FatigueUiState
import com.company.runcoach.feature.checkin.ui.model.PainFormState
import com.company.runcoach.feature.checkin.ui.model.PainUiState

@Composable
fun FatigueCheckInRoute(
    onNext: () -> Unit,
    onCancel: () -> Unit,
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val state = viewModel.fatigueState.collectAsStateWithLifecycle().value
    FatigueCheckInScreen(
        state = state,
        onFormChange = viewModel::updateFatigueForm,
        onSubmit = { viewModel.submitFatigue(onSuccess = onNext) },
        onCancel = onCancel
    )
}

@Composable
fun PainCheckInRoute(
    onFinish: (String?) -> Unit,
    onBack: () -> Unit,
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val state = viewModel.painState.collectAsStateWithLifecycle().value
    PainCheckInScreen(
        state = state,
        onFormChange = viewModel::updatePainForm,
        onSubmit = { viewModel.submitPain(onSuccess = onFinish) },
        onBack = onBack
    )
}

@Composable
fun FatigueCheckInScreen(
    state: FatigueUiState,
    onFormChange: (FatigueFormState) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    val form = state.form
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("fatigue_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Check-In", style = MaterialTheme.typography.headlineMedium)
        Text("Fatigue & readiness", style = MaterialTheme.typography.bodyMedium)

        ScoreSelector("Energy level", "energy", form.energyLevel, onSelect = { onFormChange(form.copy(energyLevel = it)) })
        ErrorText(state.fieldErrors["energyLevel"])
        ScoreSelector("Sleep quality", "sleep", form.sleepQuality, onSelect = { onFormChange(form.copy(sleepQuality = it)) })
        ErrorText(state.fieldErrors["sleepQuality"])
        ScoreSelector("Muscle soreness", "soreness", form.muscleSoreness, onSelect = { onFormChange(form.copy(muscleSoreness = it)) })
        ErrorText(state.fieldErrors["muscleSoreness"])
        ScoreSelector("Stress level", "stress", form.stressLevel, onSelect = { onFormChange(form.copy(stressLevel = it)) })
        ErrorText(state.fieldErrors["stressLevel"])

        SwitchRow("Feeling under the weather", form.illnessFlag) { onFormChange(form.copy(illnessFlag = it)) }
        SwitchRow("Too busy to recover well", form.tooBusyFlag) { onFormChange(form.copy(tooBusyFlag = it)) }
        SwitchRow("Travel fatigue today", form.travellingFlag) { onFormChange(form.copy(travellingFlag = it)) }

        OutlinedTextField(
            value = form.notes,
            onValueChange = { onFormChange(form.copy(notes = it)) },
            modifier = Modifier.fillMaxWidth().testTag("fatigue_notes_input"),
            label = { Text("Notes (optional)") }
        )

        ErrorText(state.errorMessage)

        Button(
            onClick = onSubmit,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().testTag("fatigue_submit")
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.testTag("fatigue_loading"))
            } else {
                Text("Next: pain check-in")
            }
        }
        Button(onClick = onCancel, modifier = Modifier.fillMaxWidth().testTag("fatigue_cancel")) {
            Text("Cancel")
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun PainCheckInScreen(
    state: PainUiState,
    onFormChange: (PainFormState) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val form = state.form
    val locations = listOf("LEFT_CALF", "RIGHT_CALF", "KNEE", "ANKLE", "HIP", "LOWER_BACK")
    val painTypes = listOf("SHARP", "DULL", "ACHING", "TIGHTNESS")
    val onsetContexts = listOf("DURING_RUN", "AFTER_RUN", "ALL_DAY", "OTHER")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("pain_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Check-In", style = MaterialTheme.typography.headlineMedium)
        Text("Pain signals", style = MaterialTheme.typography.bodyMedium)
        Text("This check-in helps keep training safe. It is not a diagnosis.")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = form.hasPain == false,
                onClick = { onFormChange(form.copy(hasPain = false, bodyRegion = null, painType = null, severity = null, onsetContext = null, canRun = true)) },
                label = { Text("No pain") },
                modifier = Modifier.testTag("pain_no")
            )
            FilterChip(
                selected = form.hasPain == true,
                onClick = { onFormChange(form.copy(hasPain = true, canRun = form.canRun ?: true)) },
                label = { Text("Yes, I have pain") },
                modifier = Modifier.testTag("pain_yes")
            )
        }
        ErrorText(state.fieldErrors["hasPain"])

        if (form.hasPain == true) {
            Text("Location")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                locations.forEach { location ->
                    FilterChip(
                        selected = form.bodyRegion == location,
                        onClick = { onFormChange(form.copy(bodyRegion = location)) },
                        label = { Text(location.replace('_', ' ')) },
                        modifier = Modifier.testTag("location_$location")
                    )
                }
            }
            ErrorText(state.fieldErrors["bodyRegion"])

            Text("Pain type")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                painTypes.forEach { type ->
                    FilterChip(
                        selected = form.painType == type,
                        onClick = { onFormChange(form.copy(painType = type)) },
                        label = { Text(type.replace('_', ' ')) },
                        modifier = Modifier.testTag("type_$type")
                    )
                }
            }
            ErrorText(state.fieldErrors["painType"])

            Text("Severity: ${form.severity ?: 0}")
            Slider(
                value = (form.severity ?: 0).toFloat(),
                onValueChange = { onFormChange(form.copy(severity = it.toInt())) },
                valueRange = 0f..10f,
                steps = 9,
                modifier = Modifier.testTag("pain_severity")
            )
            ErrorText(state.fieldErrors["severity"])

            Text("When did it start?")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                onsetContexts.forEach { context ->
                    FilterChip(
                        selected = form.onsetContext == context,
                        onClick = { onFormChange(form.copy(onsetContext = context)) },
                        label = { Text(context.replace('_', ' ')) },
                        modifier = Modifier.testTag("onset_$context")
                    )
                }
            }
            ErrorText(state.fieldErrors["onsetContext"])

            SwitchRow("Can you run comfortably today?", form.canRun ?: true) {
                onFormChange(form.copy(canRun = it))
            }
            ErrorText(state.fieldErrors["canRun"])
        }

        OutlinedTextField(
            value = form.notes,
            onValueChange = { onFormChange(form.copy(notes = it)) },
            modifier = Modifier.fillMaxWidth().testTag("pain_notes_input"),
            label = { Text("Notes (optional)") }
        )

        ErrorText(state.errorMessage)

        Button(
            onClick = onSubmit,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().testTag("pain_submit")
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.testTag("pain_loading"))
            } else {
                Text("Save check-in")
            }
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().testTag("pain_back")) {
            Text("Back")
        }
    }
}

@Composable
fun ReadinessSummaryRoute(readinessState: String?, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("checkin_summary_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Check-in saved", style = MaterialTheme.typography.headlineMedium)
        Text(
            when (readinessState) {
                "HIGH_RISK" -> "Thanks for checking in. Keep training easy today and reassess tomorrow."
                "CAUTION" -> "Thanks for checking in. Keep effort controlled today."
                else -> "Thanks for checking in. You’re set for today’s plan."
            }
        )
        Button(onClick = onDone, modifier = Modifier.testTag("summary_done")) {
            Text("Back to Today")
        }
    }
}

@Composable
private fun ScoreSelector(
    label: String,
    tagPrefix: String,
    selectedValue: Int?,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            (1..5).forEach { score ->
                FilterChip(
                    selected = selectedValue == score,
                    onClick = { onSelect(score) },
                    label = { Text(score.toString()) },
                    modifier = Modifier.testTag("${tagPrefix}_$score")
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ErrorText(message: String?) {
    if (message != null) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}
