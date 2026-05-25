package com.company.runcoach.feature.progress.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ProgressSummaryResponse(
    val planId: String? = null,
    val planVersion: Int? = null,
    val currentTrainingWeek: Int? = null,
    val summary: SummaryResponse? = null,
    val weeklyCompletion: List<WeeklyCompletionItem> = emptyList(),
    val longRunProgression: List<LongRunProgressionItem> = emptyList(),
    val readinessTrend: List<ReadinessTrendItem> = emptyList(),
    val recentStatusDistribution: RecentStatusDistribution? = null,
    val emptyState: Boolean = false,
    val message: String? = null
)

@Serializable
data class SummaryResponse(
    val plannedWorkouts: Int,
    val completedWorkouts: Int,
    val skippedWorkouts: Int,
    val rescheduledWorkouts: Int,
    val adherencePercentage: Int
)

@Serializable
data class WeeklyCompletionItem(
    val weekNumber: Int,
    val planned: Int,
    val completed: Int,
    val skipped: Int,
    val completionPercentage: Int
)

@Serializable
data class LongRunProgressionItem(
    val weekNumber: Int,
    val plannedDistanceKm: Double,
    val actualDistanceKm: Double? = null,
    val status: String
)

@Serializable
data class ReadinessTrendItem(
    val date: String,
    val readinessState: String,
    val fatigueLevel: Int? = null,
    val painSeverity: Int? = null
)

@Serializable
data class RecentStatusDistribution(
    val planned: Int,
    val completed: Int,
    val skipped: Int,
    val rescheduled: Int
)
