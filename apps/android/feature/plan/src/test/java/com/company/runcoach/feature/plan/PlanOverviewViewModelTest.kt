package com.company.runcoach.feature.plan

import com.company.runcoach.feature.plan.data.PlanRepository
import com.company.runcoach.feature.plan.data.remote.CurrentPlanResponse
import com.company.runcoach.feature.plan.data.remote.PlanApiService
import com.company.runcoach.feature.plan.data.remote.RaceGoalSummary
import com.company.runcoach.feature.plan.data.remote.WeekSummary
import com.company.runcoach.feature.plan.data.remote.WorkoutSummary
import com.company.runcoach.feature.plan.ui.PlanOverviewViewModel
import com.company.runcoach.feature.plan.ui.model.CalendarViewMode
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class PlanOverviewViewModelTest {

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
    fun loadSuccess_selectsCurrentWeekByDefault() = runTest(dispatcher) {
        val vm = PlanOverviewViewModel(PlanRepository(SuccessApi()), fixedClock())
        advanceUntilIdle()

        val state = vm.uiState.value

        assertEquals(2, state.selectedWeekIndex)
        assertNotNull(state.selectedWeek)
        assertTrue(state.selectedWeek!!.workouts.isNotEmpty())
        assertEquals(CalendarViewMode.WEEK, state.calendarViewMode)
        assertEquals("BUILD", state.selectedWeekPhaseLabel)
    }

    @Test
    fun loadEmpty_showsEmptyState() = runTest(dispatcher) {
        val vm = PlanOverviewViewModel(PlanRepository(NotFoundApi()), fixedClock())
        advanceUntilIdle()

        assertTrue(vm.uiState.value.emptyMessage != null)
    }

    @Test
    fun loadFailure_showsErrorState() = runTest(dispatcher) {
        val vm = PlanOverviewViewModel(PlanRepository(FailingApi()), fixedClock())
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errorMessage != null)
    }

    @Test
    fun weekNavigation_changesSelectedWeek() = runTest(dispatcher) {
        val vm = PlanOverviewViewModel(PlanRepository(SuccessApi()), fixedClock())
        advanceUntilIdle()

        vm.onNextWeek()
        assertEquals(3, vm.uiState.value.selectedWeekIndex)

        vm.onPreviousWeek()
        assertEquals(2, vm.uiState.value.selectedWeekIndex)
    }

    @Test
    fun dayMode_filtersWorkouts_andSupportsDaySwitching() = runTest(dispatcher) {
        val vm = PlanOverviewViewModel(PlanRepository(SuccessApi()), fixedClock())
        advanceUntilIdle()

        vm.onSelectDayView()
        var state = vm.uiState.value
        assertEquals(CalendarViewMode.DAY, state.calendarViewMode)
        assertTrue(state.availableDayLabels.isNotEmpty())
        assertEquals(1, state.selectedWeek?.workouts?.size)

        val alternateDay = state.availableDayLabels.last()
        vm.onSelectDay(alternateDay)
        state = vm.uiState.value
        assertEquals(alternateDay, state.selectedDayLabel)
        assertEquals(1, state.selectedWeek?.workouts?.size)
        assertEquals("w2b", state.selectedWeek?.workouts?.first()?.plannedWorkoutId)

        vm.onSelectWeekView()
        state = vm.uiState.value
        assertEquals(CalendarViewMode.WEEK, state.calendarViewMode)
        assertEquals(2, state.selectedWeek?.workouts?.size)
    }

    @Test
    fun recoveryAndPhase_areMappedForSelectedWeek() = runTest(dispatcher) {
        val vm = PlanOverviewViewModel(PlanRepository(SuccessApi()), fixedClock())
        advanceUntilIdle()

        vm.onPreviousWeek()
        val state = vm.uiState.value
        assertEquals(1, state.selectedWeekIndex)
        assertTrue(state.isRecoveryWeek)
        assertEquals("RECOVERY", state.selectedWeekPhaseLabel)
    }

    private fun fixedClock(): Clock = Clock.fixed(Instant.parse("2026-05-20T00:00:00Z"), ZoneOffset.UTC)
}

private class SuccessApi : PlanApiService {
    override suspend fun getCurrentPlan(): CurrentPlanResponse = CurrentPlanResponse(
        trainingPlanId = "plan-1",
        planVersion = 1,
        methodologyCode = "ROAD_HALF_V1",
        raceGoal = RaceGoalSummary("HALF_MARATHON", "2026-10-04"),
        currentWeekIndex = 2,
        weeks = listOf(1, 2, 3).map { index ->
            WeekSummary(
                weekIndex = index,
                phase = if (index == 1) "RECOVERY" else "BUILD",
                recoveryWeek = index == 1,
                targetDistanceKm = 30.0,
                workouts = listOf(
                    WorkoutSummary(
                        plannedWorkoutId = "w$index",
                        scheduledDate = "2026-05-2$index",
                        workoutType = "EASY_RUN",
                        status = if (index == 1) "COMPLETED" else if (index == 2) "PLANNED" else "MISSED",
                        plannedDistanceKm = 6.0,
                        plannedDurationMin = null,
                        intensityZone = "EASY"
                    ),
                    WorkoutSummary(
                        plannedWorkoutId = "w${index}b",
                        scheduledDate = "2026-05-2${index.plus(1)}",
                        workoutType = "REST_DAY",
                        status = "PLANNED",
                        plannedDistanceKm = null,
                        plannedDurationMin = 40,
                        intensityZone = null
                    )
                )
            )
        }
    )
}

private class NotFoundApi : PlanApiService {
    override suspend fun getCurrentPlan(): CurrentPlanResponse {
        throw HttpException(Response.error<CurrentPlanResponse>(404, "".toResponseBody(null)))
    }
}

private class FailingApi : PlanApiService {
    override suspend fun getCurrentPlan(): CurrentPlanResponse {
        throw IllegalStateException("network")
    }
}
