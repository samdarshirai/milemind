package com.company.runcoach.feature.onboarding.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScaffold(
    title: String,
    subtitle: String,
    lightSection: Boolean = false,
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        snackbarHost = { snackbarHostState?.let { SnackbarHost(hostState = it) } }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (lightSection) Color(0xFFFFFFFF) else Color(0xFF131313))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val titleColor = if (lightSection) Color(0xFF1B1B1B) else Color(0xFFE2E2E2)
            val subtitleColor = if (lightSection) Color(0xFF666666) else Color(0xFF959494)
            Text(
                text = "MILEMIND",
                style = MaterialTheme.typography.labelMedium,
                color = subtitleColor
            )
            Text(text = title, style = MaterialTheme.typography.headlineMedium, color = titleColor)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = subtitleColor)
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
fun StepProgressIndicator(stepIndex: Int, steps: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        repeat(steps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
                    .background(
                        if (index <= stepIndex) Color(0xFFC8F6F9) else Color(0xFF353535),
                        RoundedCornerShape(999.dp)
                    )
                    .padding(2.dp)
            )
        }
    }
}

@Composable
fun HeroBlock(title: String, subtitle: String, modifier: Modifier = Modifier, tag: String? = null) {
    Card(
        modifier = modifier.then(if (tag != null) Modifier.testTag(tag) else Modifier),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2A2E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color(0xFFE6F6F8))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFBBD9DE))
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    tag: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.then(if (tag != null) Modifier.testTag(tag) else Modifier),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = Color(0xFF1B1B1B))
            content()
        }
    }
}

@Composable
fun InfoBanner(message: String, modifier: Modifier = Modifier, tag: String? = null) {
    Card(
        modifier = modifier.then(if (tag != null) Modifier.testTag(tag) else Modifier),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCFC4C5),
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
fun SelectableChipGroup(options: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(4).forEach { rowDays ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowDays.forEach { day ->
                    FilterChip(
                        selected = selected.contains(day),
                        onClick = { onToggle(day) },
                        label = { Text(day.take(3)) }
                    )
                }
            }
        }
    }
}

@Composable
fun CtaButtonGroup(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) { content() }
}
