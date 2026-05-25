package com.company.runcoach.feature.progress.data

import com.company.runcoach.feature.progress.data.remote.ProgressApiService
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val apiService: ProgressApiService
) {
    suspend fun loadSummary(): Result<ProgressSummaryData> = runCatching {
        val response = apiService.getProgressSummary()
        ProgressSummaryData(
            isEmptyState = response.emptyState,
            message = response.message,
            summary = response.summary?.let {
                ProgressHeaderSummary(
                    adherencePercentage = it.adherencePercentage,
                    completedWorkouts = it.completedWorkouts,
                    currentTrainingWeek = response.currentTrainingWeek,
                    readinessTrend = summarizeReadinessTrend(response.readinessTrend)
                )
            },
            weeklyCompletion = response.weeklyCompletion.map {
                WeeklyCompletionData(
                    weekNumber = it.weekNumber,
                    completed = it.completed,
                    planned = it.planned,
                    completionPercentage = it.completionPercentage
                )
            },
            longRunProgression = response.longRunProgression.map {
                LongRunProgressionData(
                    weekNumber = it.weekNumber,
                    plannedDistanceKm = it.plannedDistanceKm,
                    actualDistanceKm = it.actualDistanceKm,
                    status = it.status
                )
            },
            readinessTrend = response.readinessTrend.map {
                ReadinessTrendData(
                    date = it.date,
                    readinessState = it.readinessState,
                    fatigueLevel = it.fatigueLevel,
                    painSeverity = it.painSeverity
                )
            },
            recentStatus = response.recentStatusDistribution?.let {
                RecentStatusDistributionData(
                    planned = it.planned,
                    completed = it.completed,
                    skipped = it.skipped,
                    rescheduled = it.rescheduled
                )
            }
        )
    }

    private fun summarizeReadinessTrend(items: List<com.company.runcoach.feature.progress.data.remote.ReadinessTrendItem>): String {
        if (items.isEmpty()) {
            return "No readiness trend yet"
        }
        val latest = items.last().readinessState.uppercase(Locale.US)
        return when (latest) {
            "READY" -> "Trending ready"
            "CAUTION" -> "Needs caution"
            "HIGH_RISK" -> "Protect recovery"
            else -> "Monitoring"
        }
    }
}

data class ProgressSummaryData(
    val isEmptyState: Boolean,
    val message: String?,
    val summary: ProgressHeaderSummary?,
    val weeklyCompletion: List<WeeklyCompletionData>,
    val longRunProgression: List<LongRunProgressionData>,
    val readinessTrend: List<ReadinessTrendData>,
    val recentStatus: RecentStatusDistributionData?
)

data class ProgressHeaderSummary(
    val adherencePercentage: Int,
    val completedWorkouts: Int,
    val currentTrainingWeek: Int?,
    val readinessTrend: String
)

data class WeeklyCompletionData(
    val weekNumber: Int,
    val completed: Int,
    val planned: Int,
    val completionPercentage: Int
)

data class LongRunProgressionData(
    val weekNumber: Int,
    val plannedDistanceKm: Double,
    val actualDistanceKm: Double?,
    val status: String
)

data class ReadinessTrendData(
    val date: String,
    val readinessState: String,
    val fatigueLevel: Int?,
    val painSeverity: Int?
)

data class RecentStatusDistributionData(
    val planned: Int,
    val completed: Int,
    val skipped: Int,
    val rescheduled: Int
)
