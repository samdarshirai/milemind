package com.company.runcoach.feature.racegoal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.company.runcoach.feature.racegoal.ui.model.RaceGoalForm

@Composable
fun RaceDistanceCard(selected: String?, onSelect: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().testTag("race_distance_card")) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("RACE DISTANCE", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4C4546))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("HALF_MARATHON" to "Half marathon", "MARATHON" to "Marathon").forEach { (value, label) ->
                    FilterChip(selected = selected == value, onClick = { onSelect(value) }, label = { Text(label) })
                }
            }
        }
    }
}

@Composable
fun GoalStyleSelector(selected: String?, onSelect: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().testTag("goal_style_selector")) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("GOAL STYLE", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4C4546))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "FINISH" to "FINISH",
                    "IMPROVE" to "IMPROVE",
                    "PB" to "PERSONAL BEST"
                ).forEach { (value, label) ->
                    FilterChip(selected = selected == value, onClick = { onSelect(value) }, label = { Text(label) })
                }
            }
            Text(
                "Choose Finish if this is your first race or you want a conservative plan.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4C4546)
            )
        }
    }
}

@Composable
fun TargetTimeInput(value: String, error: String?, onValueChange: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().testTag("target_time_input_card")) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("TARGET TIME (OPTIONAL)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4C4546))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("HH:MM:SS") },
                isError = error != null,
                modifier = Modifier.fillMaxWidth().testTag("target_time_input")
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun RaceGoalSummaryCard(form: RaceGoalForm) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().testTag("race_goal_summary_card")) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Review your goal", style = MaterialTheme.typography.titleMedium)
            Text("Distance: ${form.raceDistanceType?.replace('_', ' ')}")
            Text("Race name: ${form.raceName.ifBlank { "Not set" }}")
            Text("Race date: ${form.raceDate}")
            Text("Goal style: ${form.goalStyle}")
            Text("Target time: ${form.targetTime.ifBlank { "Not set" }}")
        }
    }
}
