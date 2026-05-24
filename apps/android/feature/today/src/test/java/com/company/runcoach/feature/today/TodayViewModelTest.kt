package com.company.runcoach.feature.today

import com.company.runcoach.feature.today.data.TodayInsightsData
import com.company.runcoach.feature.today.data.TodayRepository
import com.company.runcoach.feature.today.data.remote.CurrentPlanResponse
import com.company.runcoach.feature.today.data.remote.TodayApiService
import com.company.runcoach.feature.today.data.remote.TodayInsightsResponse
import com.company.runcoach.feature.today.data.remote.WeekSummary
import com.company.runcoach.feature.today.ui.model.ReadinessBannerStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

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
    fun readinessBanner_noCheckInState() {
        val model = mapReadinessBanner(
            TodayInsightsData(
                date = "2026-06-15",
                readinessState = null,
                readinessLabel = null,
                readinessMessage = null,
                hasCheckInToday = false,
                todayWorkout = null
            )
        )

        assertEquals(ReadinessBannerStatus.NO_CHECK_IN, model.status)
        assertEquals("Check in", model.ctaLabel)
    }

    @Test
    fun readinessBanner_readyState() {
        val model = mapReadinessBanner(
            TodayInsightsData(
                date = "2026-06-15",
                readinessState = "READY",
                readinessLabel = "Ready",
                readinessMessage = "You are set for your planned effort.",
                hasCheckInToday = true,
                todayWorkout = null
            )
        )

        assertEquals(ReadinessBannerStatus.READY, model.status)
        assertEquals("Update readiness", model.ctaLabel)
    }

    @Test
    fun readinessBanner_cautionState() {
        val model = mapReadinessBanner(
            TodayInsightsData(
                date = "2026-06-15",
                readinessState = "CAUTION",
                readinessLabel = "Caution",
                readinessMessage = "Keep effort conservative.",
                hasCheckInToday = true,
                todayWorkout = null
            )
        )

        assertEquals(ReadinessBannerStatus.CAUTION, model.status)
    }

    @Test
    fun readinessBanner_highRiskState() {
        val model = mapReadinessBanner(
            TodayInsightsData(
                date = "2026-06-15",
                readinessState = "HIGH_RISK",
                readinessLabel = "Take it easy",
                readinessMessage = "Downshift today.",
                hasCheckInToday = true,
                todayWorkout = null
            )
        )

        assertEquals(ReadinessBannerStatus.HIGH_RISK, model.status)
    }

    @Test
    fun apiErrorHandling_setsErrorBanner() = runTest {
        val vm = TodayViewModel(TodayRepository(FailingTodayApi()))
        advanceUntilIdle()

        assertEquals(ReadinessBannerStatus.ERROR, vm.uiState.value.readinessBanner.status)
        assertTrue(vm.uiState.value.errorMessage?.contains("Could not load") == true)
    }

    @Test
    fun refreshAfterCheckIn_fetchesLatestReadiness() = runTest {
        val vm = TodayViewModel(TodayRepository(RefreshingTodayApi()))
        advanceUntilIdle()
        assertEquals(ReadinessBannerStatus.NO_CHECK_IN, vm.uiState.value.readinessBanner.status)

        vm.refreshAfterCheckIn()
        advanceUntilIdle()

        assertEquals(ReadinessBannerStatus.READY, vm.uiState.value.readinessBanner.status)
    }

    @Test
    fun refreshAfterCheckIn_handlesHighRiskBanner() = runTest {
        val vm = TodayViewModel(TodayRepository(HighRiskRefreshApi()))
        advanceUntilIdle()
        assertEquals(ReadinessBannerStatus.NO_CHECK_IN, vm.uiState.value.readinessBanner.status)

        vm.refreshAfterCheckIn()
        advanceUntilIdle()

        assertEquals(ReadinessBannerStatus.HIGH_RISK, vm.uiState.value.readinessBanner.status)
        assertEquals("Update readiness", vm.uiState.value.readinessBanner.ctaLabel)
    }

    @Test
    fun refreshAfterCheckIn_failureShowsRetryCopyAndCta() = runTest {
        val vm = TodayViewModel(TodayRepository(RefreshFailureApi()))
        advanceUntilIdle()
        assertEquals(ReadinessBannerStatus.READY, vm.uiState.value.readinessBanner.status)

        vm.refreshAfterCheckIn()
        advanceUntilIdle()

        assertEquals(ReadinessBannerStatus.ERROR, vm.uiState.value.readinessBanner.status)
        assertEquals("Retry", vm.uiState.value.readinessBanner.ctaLabel)
        assertEquals("Readiness was saved, but refresh failed. Tap Retry.", vm.uiState.value.errorMessage)
    }

    @Test
    fun planFetchFailureStillShowsReadinessBanner() = runTest {
        val vm = TodayViewModel(TodayRepository(InsightsSuccessPlanFailureApi()))
        advanceUntilIdle()

        assertEquals(ReadinessBannerStatus.CAUTION, vm.uiState.value.readinessBanner.status)
        assertEquals(null, vm.uiState.value.todayWorkout)
        assertTrue(vm.uiState.value.workoutLoadFailed)
        assertEquals("Today's workout could not be loaded. You can retry.", vm.uiState.value.workoutErrorMessage)
    }

    @Test
    fun retryWorkoutLoad_successClearsWorkoutError() = runTest {
        val vm = TodayViewModel(TodayRepository(PlanRetrySuccessApi()))
        advanceUntilIdle()

        assertTrue(vm.uiState.value.workoutLoadFailed)
        vm.retryWorkoutLoad()
        advanceUntilIdle()

        assertEquals(false, vm.uiState.value.workoutLoadFailed)
        assertEquals(null, vm.uiState.value.workoutErrorMessage)
        assertTrue(vm.uiState.value.todayWorkout != null)
    }

    private class FailingTodayApi : TodayApiService {
        override suspend fun getTodayInsights(): TodayInsightsResponse {
            error("network")
        }

        override suspend fun getCurrentPlan(): CurrentPlanResponse {
            return emptyPlan()
        }
    }

    private class RefreshingTodayApi : TodayApiService {
        private var callCount: Int = 0
        override suspend fun getTodayInsights(): TodayInsightsResponse {
            callCount += 1
            return if (callCount == 1) {
                TodayInsightsResponse(
                    date = "2026-06-15",
                    readinessState = null,
                    readinessLabel = null,
                    readinessMessage = null,
                    hasCheckInToday = false
                )
            } else {
                TodayInsightsResponse(
                    date = "2026-06-15",
                    readinessState = "READY",
                    readinessLabel = "Ready",
                    readinessMessage = "You are set for planned training.",
                    hasCheckInToday = true
                )
            }
        }

        override suspend fun getCurrentPlan(): CurrentPlanResponse {
            return emptyPlan()
        }
    }

    private class HighRiskRefreshApi : TodayApiService {
        private var callCount: Int = 0
        override suspend fun getTodayInsights(): TodayInsightsResponse {
            callCount += 1
            return if (callCount == 1) {
                TodayInsightsResponse(
                    date = "2026-06-15",
                    readinessState = null,
                    readinessLabel = null,
                    readinessMessage = null,
                    hasCheckInToday = false
                )
            } else {
                TodayInsightsResponse(
                    date = "2026-06-15",
                    readinessState = "HIGH_RISK",
                    readinessLabel = "High risk",
                    readinessMessage = "Risk signals are elevated. Keep training load conservative today.",
                    hasCheckInToday = true
                )
            }
        }

        override suspend fun getCurrentPlan(): CurrentPlanResponse {
            return emptyPlan()
        }
    }

    private class InsightsSuccessPlanFailureApi : TodayApiService {
        override suspend fun getTodayInsights(): TodayInsightsResponse {
            return TodayInsightsResponse(
                date = "2026-06-15",
                readinessState = "CAUTION",
                readinessLabel = "Caution",
                readinessMessage = "Keep effort conservative.",
                hasCheckInToday = true
            )
        }

        override suspend fun getCurrentPlan(): CurrentPlanResponse {
            throw HttpException(
                Response.error<CurrentPlanResponse>(
                    500,
                    "{}".toResponseBody("application/json".toMediaType())
                )
            )
        }
    }

    private class PlanRetrySuccessApi : TodayApiService {
        private var planCallCount: Int = 0
        override suspend fun getTodayInsights(): TodayInsightsResponse {
            return TodayInsightsResponse(
                date = "2026-06-15",
                readinessState = "CAUTION",
                readinessLabel = "Caution",
                readinessMessage = "Keep effort conservative.",
                hasCheckInToday = true
            )
        }

        override suspend fun getCurrentPlan(): CurrentPlanResponse {
            planCallCount += 1
            if (planCallCount == 1) {
                throw HttpException(
                    Response.error<CurrentPlanResponse>(
                        500,
                        "{}".toResponseBody("application/json".toMediaType())
                    )
                )
            }
            return CurrentPlanResponse(
                trainingPlanId = "plan-1",
                currentWeekIndex = 1,
                weeks = listOf(
                    WeekSummary(
                        weekIndex = 1,
                        workouts = listOf(
                            com.company.runcoach.feature.today.data.remote.WorkoutSummary(
                                plannedWorkoutId = "w-1",
                                scheduledDate = "2026-06-15",
                                workoutType = "EASY_RUN",
                                status = "PLANNED",
                                plannedDurationMin = 45
                            )
                        )
                    )
                )
            )
        }
    }

    private class RefreshFailureApi : TodayApiService {
        private var insightCallCount: Int = 0

        override suspend fun getTodayInsights(): TodayInsightsResponse {
            insightCallCount += 1
            if (insightCallCount == 1) {
                return TodayInsightsResponse(
                    date = "2026-06-15",
                    readinessState = "READY",
                    readinessLabel = "Ready",
                    readinessMessage = "You are set for planned training.",
                    hasCheckInToday = true
                )
            }
            error("refresh failed")
        }

        override suspend fun getCurrentPlan(): CurrentPlanResponse {
            return emptyPlan()
        }
    }

    companion object {
        private fun emptyPlan() = CurrentPlanResponse(
            trainingPlanId = "plan-1",
            currentWeekIndex = 1,
            weeks = emptyList<WeekSummary>()
        )
    }
}
