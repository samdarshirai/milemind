package com.company.runcoach.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.runcoach.feature.profile.data.EditableProfile
import androidx.compose.material3.LocalContentColor

private val weekdays = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
private val strengthOptions = listOf(0, 1, 2)
private val unitsOptions = listOf("KM", "MILES")

@Composable
fun ProfileEditRoute(
    onOpenStrava: () -> Unit,
    viewModel: ProfileEditViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    ProfileEditScreen(
        state = state,
        onProfileChange = viewModel::updateProfile,
        onSave = viewModel::save,
        onOpenStrava = onOpenStrava
    )
}

@Composable
fun ProfileEditScreen(
    state: ProfileEditUiState,
    onProfileChange: (EditableProfile) -> Unit,
    onSave: () -> Unit,
    onOpenStrava: () -> Unit = {}
) {
    val profile = state.profile
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProfileHeroBlock(
            title = "Edit profile",
            subtitle = "Some changes may update your future workouts, but completed workouts will not be changed.",
            tag = "profile_edit_hero_block"
        )
        ProfileSectionCard(title = "Onboarding baseline", tag = "profile_baseline_card") {
            Text("Weekly distance: ${profile.weeklyDistance}", style = MaterialTheme.typography.bodyMedium)
            Text("Longest recent run: ${profile.longestRun}", style = MaterialTheme.typography.bodyMedium)
        }

        ProfileSectionCard(title = "Training schedule", tag = "profile_schedule_card") {
            Text("Preferred run days")
            weekdays.forEach { day ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(
                        checked = day in profile.preferredRunDays,
                        onCheckedChange = { checked ->
                            val nextDays = if (checked) {
                                profile.preferredRunDays + day
                            } else {
                                profile.preferredRunDays - day
                            }
                            onProfileChange(profile.copy(preferredRunDays = nextDays))
                        },
                        modifier = Modifier.testTag("runDay-$day")
                    )
                    Text(day)
                }
            }
            state.fieldErrors["preferredRunDays"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Text("Long run day")
            weekdays.forEach { day ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RadioButton(
                        selected = profile.preferredLongRunDay == day,
                        onClick = { onProfileChange(profile.copy(preferredLongRunDay = day)) },
                        modifier = Modifier.testTag("longRunDay-$day")
                    )
                    Text(day)
                }
            }
            state.fieldErrors["preferredLongRunDay"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }

        ProfileSectionCard(title = "Preferences", tag = "profile_preferences_card") {
            Text("Strength days per week")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                strengthOptions.forEach { option ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        RadioButton(
                            selected = profile.strengthDaysPerWeek == option,
                            onClick = { onProfileChange(profile.copy(strengthDaysPerWeek = option)) },
                            modifier = Modifier.testTag("strength-$option")
                        )
                        Text(option.toString())
                    }
                }
            }
            state.fieldErrors["strengthDaysPerWeek"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Text("Units")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                unitsOptions.forEach { option ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        RadioButton(
                            selected = profile.units == option,
                            onClick = { onProfileChange(profile.copy(units = option)) },
                            modifier = Modifier.testTag("units-$option")
                        )
                        Text(option)
                    }
                }
            }
            state.fieldErrors["units"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            OutlinedTextField(
                value = profile.timezone,
                onValueChange = { onProfileChange(profile.copy(timezone = it)) },
                label = { Text("Timezone") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("timezoneInput")
            )
            state.fieldErrors["timezone"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }

        ProfileSectionCard(title = "Injury context", tag = "profile_injury_card") {
            OutlinedTextField(
                value = profile.injuryHistorySummary,
                onValueChange = { onProfileChange(profile.copy(injuryHistorySummary = it)) },
                label = { Text("Injury history summary") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("injurySummaryInput")
            )
            Text("Running injury in last 12 months")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Yes" to true, "No" to false).forEach { (label, value) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        RadioButton(
                            selected = profile.hadRunningInjuryLast12Months == value,
                            onClick = { onProfileChange(profile.copy(hadRunningInjuryLast12Months = value)) }
                        )
                        Text(label)
                    }
                }
            }
            state.fieldErrors["injuryHistory.hadRunningInjuryLast12Months"]?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            state.fieldErrors["injuryHistory.summary"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }

        ProfileSectionCard(title = "Integrations", tag = "profile_integrations_card") {
            Text("Strava connection status and account controls")
            Button(
                onClick = onOpenStrava,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_open_strava_button")
            ) {
                Text("Open Strava integration")
            }
        }

        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_cta_group"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSave,
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saveButton")
            ) {
                if (state.isSaving) CircularProgressIndicator(modifier = Modifier.padding(2.dp)) else Text("Save changes")
            }
        }
        if (state.saveSuccess) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE9FFF0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Saved. Your profile changes are now active.", modifier = Modifier.padding(10.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeroBlock(title: String, subtitle: String, tag: String) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag(tag),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101216)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, color = Color(0xFFE6F6F8))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFBBD9DE))
        }
    }
}

@Composable
private fun ProfileSectionCard(title: String, tag: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag(tag),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16181D)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = Color(0xFFCFD3DA))
            CompositionLocalProvider(LocalContentColor provides Color(0xFFE6E8EC)) {
                content()
            }
        }
    }
}
