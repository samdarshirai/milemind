package com.company.runcoach.feature.workout

import androidx.lifecycle.SavedStateHandle
import com.company.runcoach.feature.workout.data.WorkoutRepository
import com.company.runcoach.feature.workout.data.remote.PlannedWorkoutDetailResponse
import com.company.runcoach.feature.workout.data.remote.WorkoutApiService
import com.company.runcoach.feature.workout.ui.WorkoutDetailViewModel
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.JsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutDetailViewModelTest {

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
    fun loadSuccess_mapsWorkoutInfoAndBackendStatus() = runTest(dispatcher) {
        val vm = WorkoutDetailViewModel(
            repository = WorkoutRepository(SuccessApi(status = "COMPLETED")),
            savedStateHandle = SavedStateHandle(mapOf("plannedWorkoutId" to "workout-1"))
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("TEMPO RUN", state.workoutType)
        assertEquals("Completed", state.statusLabel)
        assertTrue(state.instructions.isNotBlank())
    }

    @Test
    fun loadSuccess_usesNavigationFallbackStatusWhenBackendMissing() = runTest(dispatcher) {
        val vm = WorkoutDetailViewModel(
            repository = WorkoutRepository(SuccessApi(status = null)),
            savedStateHandle = SavedStateHandle(mapOf("plannedWorkoutId" to "workout-1", "status" to "REST_DAY"))
        )
        advanceUntilIdle()

        assertEquals("Rest day", vm.uiState.value.statusLabel)
    }

    @Test
    fun loadSuccess_unknownStatusFallsBackToUnknownLabel() = runTest(dispatcher) {
        val vm = WorkoutDetailViewModel(
            repository = WorkoutRepository(SuccessApi(status = "SOMETHING_NEW")),
            savedStateHandle = SavedStateHandle(mapOf("plannedWorkoutId" to "workout-1"))
        )
        advanceUntilIdle()

        assertEquals("Unknown", vm.uiState.value.statusLabel)
    }

    @Test
    fun loadFailure_showsError() = runTest(dispatcher) {
        val vm = WorkoutDetailViewModel(
            repository = WorkoutRepository(FailingApi()),
            savedStateHandle = SavedStateHandle(mapOf("plannedWorkoutId" to "workout-1"))
        )
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errorMessage != null)
    }
}

private class SuccessApi(private val status: String?) : WorkoutApiService {
    override suspend fun getWorkoutDetail(plannedWorkoutId: String): PlannedWorkoutDetailResponse {
        return PlannedWorkoutDetailResponse(
            plannedWorkoutId = plannedWorkoutId,
            scheduledDate = LocalDate.parse("2026-05-20").toString(),
            workoutType = "TEMPO_RUN",
            workoutSubtype = "Tempo Run",
            status = status,
            plannedDistanceKm = 8.0,
            plannedDurationMin = 50,
            intensityZone = "MODERATE",
            structure = listOf(mapOf("step" to JsonPrimitive("Warm-up"))),
            whyThisWorkout = "Build threshold endurance.",
            changeReasonCodes = emptyList()
        )
    }
}

private class FailingApi : WorkoutApiService {
    override suspend fun getWorkoutDetail(plannedWorkoutId: String): PlannedWorkoutDetailResponse {
        throw IllegalStateException("network")
    }
}
