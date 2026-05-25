package com.company.runcoach.feature.progress

import com.company.runcoach.feature.progress.data.ProgressRepository
import com.company.runcoach.feature.progress.data.remote.ProgressApiService
import com.company.runcoach.feature.progress.data.remote.ProgressSummaryResponse
import com.company.runcoach.feature.progress.data.remote.ReadinessTrendItem
import com.company.runcoach.feature.progress.data.remote.RecentStatusDistribution
import com.company.runcoach.feature.progress.data.remote.SummaryResponse
import com.company.runcoach.feature.progress.data.remote.WeeklyCompletionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadingToDataRichState() = runTest {
        val vm = ProgressViewModel(ProgressRepository(DataRichApi()))
        advanceUntilIdle()
        val state = vm.uiState.value

        assertEquals(false, state.isLoading)
        assertNotNull(state.content)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun emptyStateWhenBackendMarksEmpty() = runTest {
        val vm = ProgressViewModel(ProgressRepository(EmptyApi()))
        advanceUntilIdle()
        val state = vm.uiState.value

        assertEquals(false, state.isLoading)
        assertTrue(state.emptyStateMessage?.contains("No active plan") == true)
    }

    @Test
    fun errorStateOnFailure() = runTest {
        val vm = ProgressViewModel(ProgressRepository(FailingApi()))
        advanceUntilIdle()
        val state = vm.uiState.value

        assertEquals(false, state.isLoading)
        assertEquals("Could not load progress summary.", state.errorMessage)
    }

    @Test
    fun readinessTrendRendersNullPainWithoutFallbackNoise() = runTest {
        val vm = ProgressViewModel(ProgressRepository(NullPainTrendApi()))
        advanceUntilIdle()
        val detail = vm.uiState.value.content?.readinessTrend?.firstOrNull()?.detail

        assertEquals("CAUTION · fatigue 3 · pain --", detail)
    }

    private class DataRichApi : ProgressApiService {
        override suspend fun getProgressSummary(): ProgressSummaryResponse {
            return ProgressSummaryResponse(
                planId = "p1",
                planVersion = 2,
                currentTrainingWeek = 4,
                summary = SummaryResponse(10, 6, 2, 1, 60),
                weeklyCompletion = listOf(WeeklyCompletionItem(4, 4, 3, 1, 75)),
                longRunProgression = listOf(
                    com.company.runcoach.feature.progress.data.remote.LongRunProgressionItem(
                        weekNumber = 4,
                        plannedDistanceKm = 18.0,
                        actualDistanceKm = 16.5,
                        status = "COMPLETED"
                    )
                ),
                readinessTrend = listOf(ReadinessTrendItem("2026-06-10", "READY", 2, 1)),
                recentStatusDistribution = RecentStatusDistribution(3, 6, 1, 0),
                emptyState = false,
                message = "Consistency is improving."
            )
        }
    }

    private class EmptyApi : ProgressApiService {
        override suspend fun getProgressSummary(): ProgressSummaryResponse {
            return ProgressSummaryResponse(
                emptyState = true,
                message = "No active plan yet."
            )
        }
    }

    private class FailingApi : ProgressApiService {
        override suspend fun getProgressSummary(): ProgressSummaryResponse {
            error("network")
        }
    }

    private class NullPainTrendApi : ProgressApiService {
        override suspend fun getProgressSummary(): ProgressSummaryResponse {
            return ProgressSummaryResponse(
                planId = "p2",
                planVersion = 1,
                currentTrainingWeek = 1,
                summary = SummaryResponse(2, 1, 0, 0, 50),
                weeklyCompletion = listOf(WeeklyCompletionItem(1, 2, 1, 0, 50)),
                longRunProgression = emptyList(),
                readinessTrend = listOf(ReadinessTrendItem("2026-06-10", "CAUTION", 3, null)),
                recentStatusDistribution = RecentStatusDistribution(1, 1, 0, 0),
                emptyState = false,
                message = "Good start"
            )
        }
    }
}
