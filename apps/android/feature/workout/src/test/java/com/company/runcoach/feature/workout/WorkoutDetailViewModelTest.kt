package com.company.runcoach.feature.workout

import androidx.lifecycle.SavedStateHandle
import com.company.runcoach.feature.workout.data.WorkoutRepository
import com.company.runcoach.feature.workout.data.remote.AdaptationMutationResponse
import com.company.runcoach.feature.workout.data.remote.PlannedWorkoutDetailResponse
import com.company.runcoach.feature.workout.data.remote.RescheduleWorkoutRequest
import com.company.runcoach.feature.workout.data.remote.SkipWorkoutRequest
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

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
        assertFalse(state.canMarkSkipped)
        assertFalse(state.canReschedule)
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

    @Test
    fun submitSkip_usesLoadedPlanVersionAndUpdatesState() = runTest(dispatcher) {
        val api = SuccessApi(status = "PLANNED", mutationPlanVersion = 4)
        val vm = WorkoutDetailViewModel(
            repository = WorkoutRepository(api),
            savedStateHandle = SavedStateHandle(mapOf("plannedWorkoutId" to "workout-1"))
        )
        advanceUntilIdle()
        assertEquals(3, vm.uiState.value.latestPlanVersion)

        vm.openSkipSheet()
        vm.submitSkip()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.showSkipSheet)
        assertEquals(4, vm.uiState.value.latestPlanVersion)
        assertEquals(null, vm.uiState.value.mutationError)
        assertEquals(3, api.lastSkipExpectedPlanVersion)
    }

    @Test
    fun submitReschedule_usesMutationPlanVersionInState() = runTest(dispatcher) {
        val api = SuccessApi(status = "PLANNED", mutationPlanVersion = 7)
        val vm = WorkoutDetailViewModel(
            repository = WorkoutRepository(api),
            savedStateHandle = SavedStateHandle(mapOf("plannedWorkoutId" to "workout-1"))
        )
        advanceUntilIdle()

        vm.openRescheduleSheet()
        vm.updateRescheduleDate("2026-05-21")
        vm.submitReschedule()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.showRescheduleSheet)
        assertEquals(7, vm.uiState.value.latestPlanVersion)
        assertEquals(null, vm.uiState.value.mutationError)
    }

    @Test
    fun loadSuccess_plannedStatusEnablesMutationActions() = runTest(dispatcher) {
        val vm = WorkoutDetailViewModel(
            repository = WorkoutRepository(SuccessApi(status = "PLANNED")),
            savedStateHandle = SavedStateHandle(mapOf("plannedWorkoutId" to "workout-1"))
        )
        advanceUntilIdle()

        assertTrue(vm.uiState.value.canMarkSkipped)
        assertTrue(vm.uiState.value.canReschedule)
    }

    @Test
    fun submitReschedule_blankDateShowsValidationError() = runTest(dispatcher) {
        val vm = WorkoutDetailViewModel(
            repository = WorkoutRepository(SuccessApi(status = "PLANNED")),
            savedStateHandle = SavedStateHandle(mapOf("plannedWorkoutId" to "workout-1"))
        )
        advanceUntilIdle()

        vm.updateRescheduleDate("")
        vm.submitReschedule()

        assertEquals("Please choose a target date.", vm.uiState.value.mutationError)
    }

    @Test
    fun retryAfterConflict_refreshesVersionBeforeRetryingSkip() = runTest(dispatcher) {
        val api = ConflictThenSuccessApi()
        val vm = WorkoutDetailViewModel(
            repository = WorkoutRepository(api),
            savedStateHandle = SavedStateHandle(mapOf("plannedWorkoutId" to "workout-1"))
        )
        advanceUntilIdle()
        assertEquals(3, vm.uiState.value.latestPlanVersion)

        vm.openSkipSheet()
        vm.submitSkip()
        advanceUntilIdle()
        assertEquals("Your plan changed recently. Refresh to continue.", vm.uiState.value.conflictMessage)

        vm.retryAfterConflict()
        advanceUntilIdle()

        assertEquals(null, vm.uiState.value.conflictMessage)
        assertEquals(5, api.lastSkipExpectedPlanVersion)
    }

    @Test
    fun retryAfterConflict_refreshesVersionBeforeRetryingReschedule() = runTest(dispatcher) {
        val api = RescheduleConflictThenSuccessApi()
        val vm = WorkoutDetailViewModel(
            repository = WorkoutRepository(api),
            savedStateHandle = SavedStateHandle(mapOf("plannedWorkoutId" to "workout-1"))
        )
        advanceUntilIdle()
        assertEquals(3, vm.uiState.value.latestPlanVersion)

        vm.openRescheduleSheet()
        vm.updateRescheduleDate("2026-05-21")
        vm.submitReschedule()
        advanceUntilIdle()
        assertEquals("Your plan changed recently. Refresh to continue.", vm.uiState.value.conflictMessage)

        vm.retryAfterConflict()
        advanceUntilIdle()

        assertEquals(null, vm.uiState.value.conflictMessage)
        assertEquals(5, api.lastRescheduleExpectedPlanVersion)
    }
}

private class SuccessApi(
    private val status: String?,
    private val mutationPlanVersion: Int = 2
) : WorkoutApiService {
    var lastSkipExpectedPlanVersion: Int? = null

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
            changeReasonCodes = emptyList(),
            planVersion = 3
        )
    }

    override suspend fun skipWorkout(plannedWorkoutId: String, request: SkipWorkoutRequest): AdaptationMutationResponse {
        lastSkipExpectedPlanVersion = request.expectedPlanVersion
        return AdaptationMutationResponse(planVersion = mutationPlanVersion, adaptation = null)
    }

    override suspend fun rescheduleWorkout(plannedWorkoutId: String, request: RescheduleWorkoutRequest): AdaptationMutationResponse {
        return AdaptationMutationResponse(planVersion = mutationPlanVersion, adaptation = null)
    }
}

private class FailingApi : WorkoutApiService {
    override suspend fun getWorkoutDetail(plannedWorkoutId: String): PlannedWorkoutDetailResponse {
        throw IllegalStateException("network")
    }

    override suspend fun skipWorkout(plannedWorkoutId: String, request: SkipWorkoutRequest): AdaptationMutationResponse {
        throw IllegalStateException("network")
    }

    override suspend fun rescheduleWorkout(plannedWorkoutId: String, request: RescheduleWorkoutRequest): AdaptationMutationResponse {
        throw IllegalStateException("network")
    }
}

private class ConflictThenSuccessApi : WorkoutApiService {
    var detailCalls = 0
    var skipCalls = 0
    var lastSkipExpectedPlanVersion: Int? = null

    override suspend fun getWorkoutDetail(plannedWorkoutId: String): PlannedWorkoutDetailResponse {
        detailCalls += 1
        val planVersion = if (detailCalls == 1) 3 else 5
        return PlannedWorkoutDetailResponse(
            plannedWorkoutId = plannedWorkoutId,
            scheduledDate = LocalDate.parse("2026-05-20").toString(),
            workoutType = "TEMPO_RUN",
            workoutSubtype = "Tempo Run",
            status = "PLANNED",
            plannedDistanceKm = 8.0,
            plannedDurationMin = 50,
            intensityZone = "MODERATE",
            structure = listOf(mapOf("step" to JsonPrimitive("Warm-up"))),
            whyThisWorkout = "Build threshold endurance.",
            changeReasonCodes = emptyList(),
            planVersion = planVersion
        )
    }

    override suspend fun skipWorkout(plannedWorkoutId: String, request: SkipWorkoutRequest): AdaptationMutationResponse {
        skipCalls += 1
        lastSkipExpectedPlanVersion = request.expectedPlanVersion
        if (skipCalls == 1) {
            val body = """{"error":{"code":"STALE_PLAN_VERSION","message":"stale"}}"""
                .toResponseBody("application/json".toMediaType())
            throw HttpException(Response.error<Any>(409, body))
        }
        return AdaptationMutationResponse(planVersion = 6, adaptation = null)
    }

    override suspend fun rescheduleWorkout(plannedWorkoutId: String, request: RescheduleWorkoutRequest): AdaptationMutationResponse {
        return AdaptationMutationResponse(planVersion = 6, adaptation = null)
    }
}

private class RescheduleConflictThenSuccessApi : WorkoutApiService {
    var detailCalls = 0
    var rescheduleCalls = 0
    var lastRescheduleExpectedPlanVersion: Int? = null

    override suspend fun getWorkoutDetail(plannedWorkoutId: String): PlannedWorkoutDetailResponse {
        detailCalls += 1
        val planVersion = if (detailCalls == 1) 3 else 5
        return PlannedWorkoutDetailResponse(
            plannedWorkoutId = plannedWorkoutId,
            scheduledDate = LocalDate.parse("2026-05-20").toString(),
            workoutType = "TEMPO_RUN",
            workoutSubtype = "Tempo Run",
            status = "PLANNED",
            plannedDistanceKm = 8.0,
            plannedDurationMin = 50,
            intensityZone = "MODERATE",
            structure = listOf(mapOf("step" to JsonPrimitive("Warm-up"))),
            whyThisWorkout = "Build threshold endurance.",
            changeReasonCodes = emptyList(),
            planVersion = planVersion
        )
    }

    override suspend fun skipWorkout(plannedWorkoutId: String, request: SkipWorkoutRequest): AdaptationMutationResponse {
        return AdaptationMutationResponse(planVersion = 6, adaptation = null)
    }

    override suspend fun rescheduleWorkout(plannedWorkoutId: String, request: RescheduleWorkoutRequest): AdaptationMutationResponse {
        rescheduleCalls += 1
        lastRescheduleExpectedPlanVersion = request.expectedPlanVersion
        if (rescheduleCalls == 1) {
            val body = """{"error":{"code":"STALE_PLAN_VERSION","message":"stale"}}"""
                .toResponseBody("application/json".toMediaType())
            throw HttpException(Response.error<Any>(409, body))
        }
        return AdaptationMutationResponse(planVersion = 6, adaptation = null)
    }
}
