package com.company.runcoach.feature.today.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.company.runcoach.feature.today.ui.model.TodayUiState

@Composable
fun TodayRoute() {
    TodayScreen(state = TodayUiState())
}

@Composable
fun TodayScreen(state: TodayUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("today_shell"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF171B24))) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = state.title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Text(text = state.subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC8CDD9))
            }
        }
    }
}
