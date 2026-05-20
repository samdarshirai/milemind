package com.company.runcoach.feature.racegoal

import com.company.runcoach.feature.racegoal.data.RaceGoalRepository
import com.company.runcoach.feature.racegoal.data.remote.CreateRaceGoalRequest
import com.company.runcoach.feature.racegoal.data.remote.CreateRaceGoalResponse
import com.company.runcoach.feature.racegoal.data.remote.CurrentRaceGoalResponse
import com.company.runcoach.feature.racegoal.data.remote.RaceGoalApiService
import com.company.runcoach.feature.racegoal.ui.RaceGoalViewModel
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class RaceGoalViewModelTest {

    @Test
    fun missingRequiredFields_showValidationErrors() = runTest {
        val vm = RaceGoalViewModel(RaceGoalRepository(FakeApi()))
        vm.updateForm(vm.uiState.value.form.copy(raceDate = ""))

        vm.continueToReview()

        assertTrue(vm.uiState.value.fieldErrors.containsKey("raceDistanceType"))
        assertTrue(vm.uiState.value.fieldErrors.containsKey("raceDate"))
        assertTrue(vm.uiState.value.fieldErrors.containsKey("goalStyle"))
    }

    @Test
    fun raceDistanceSelection_updatesUiState() = runTest {
        val vm = RaceGoalViewModel(RaceGoalRepository(FakeApi()))

        vm.updateForm(vm.uiState.value.form.copy(raceDistanceType = "MARATHON"))

        assertEquals("MARATHON", vm.uiState.value.form.raceDistanceType)
    }

    @Test
    fun goalStyleSelection_updatesUiState() = runTest {
        val vm = RaceGoalViewModel(RaceGoalRepository(FakeApi()))

        vm.updateForm(vm.uiState.value.form.copy(goalStyle = "PB"))

        assertEquals("PB", vm.uiState.value.form.goalStyle)
    }

    @Test
    fun targetTimeOptional_isAcceptedWhenBlank() = runTest {
        val vm = RaceGoalViewModel(RaceGoalRepository(FakeApi()))
        vm.updateForm(
            vm.uiState.value.form.copy(
                raceDistanceType = "HALF_MARATHON",
                raceDate = "2099-12-30",
                goalStyle = "FINISH",
                targetTime = ""
            )
        )

        vm.continueToReview()

        assertEquals(emptyMap<String, String>(), vm.uiState.value.fieldErrors)
    }

    @Test
    fun invalidTargetTime_showsValidationError() = runTest {
        val vm = RaceGoalViewModel(RaceGoalRepository(FakeApi()))
        vm.updateForm(
            vm.uiState.value.form.copy(
                raceDistanceType = "HALF_MARATHON",
                raceDate = "2099-12-30",
                goalStyle = "FINISH",
                targetTime = "10:90:00"
            )
        )

        vm.continueToReview()

        assertTrue(vm.uiState.value.fieldErrors.containsKey("targetTimeSeconds"))
    }

    @Test
    fun utcBoundaryDate_isAcceptedByLocalValidation() = runTest {
        val vm = RaceGoalViewModel(RaceGoalRepository(FakeApi()))
        val boundaryDate = LocalDate.now(ZoneId.of("UTC")).plusWeeks(8).toString()
        vm.updateForm(
            vm.uiState.value.form.copy(
                raceDistanceType = "HALF_MARATHON",
                raceDate = boundaryDate,
                goalStyle = "FINISH",
                targetTime = ""
            )
        )

        vm.continueToReview()

        assertTrue(!vm.uiState.value.fieldErrors.containsKey("raceDate"))
    }

    @Test
    fun raceDateTooSoonBackendError_mapsFriendlyState() = runTest {
        val vm = RaceGoalViewModel(RaceGoalRepository(TooSoonApi()))
        vm.updateForm(
            vm.uiState.value.form.copy(
                raceDistanceType = "MARATHON",
                raceDate = "2099-12-30",
                goalStyle = "IMPROVE"
            )
        )
        vm.continueToReview()

        vm.saveGoal()

        assertTrue(vm.uiState.value.tooSoonMessage != null)
        assertEquals("SETUP", vm.uiState.value.step.name)
    }

    @Test
    fun existingActiveGoalError_displaysClearly() = runTest {
        val vm = RaceGoalViewModel(RaceGoalRepository(ActiveGoalConflictApi()))
        vm.updateForm(
            vm.uiState.value.form.copy(
                raceDistanceType = "HALF_MARATHON",
                raceDate = "2099-12-30",
                goalStyle = "FINISH"
            )
        )
        vm.continueToReview()

        vm.saveGoal()

        assertTrue(vm.uiState.value.activeGoalMessage?.contains("active goal") == true)
    }

    @Test
    fun successSave_transitionsToSavedState() = runTest {
        val vm = RaceGoalViewModel(RaceGoalRepository(FakeApi()))
        vm.updateForm(
            vm.uiState.value.form.copy(
                raceDistanceType = "HALF_MARATHON",
                raceDate = "2099-12-30",
                goalStyle = "FINISH",
                targetTime = "01:45:00"
            )
        )
        vm.continueToReview()

        vm.saveGoal()

        assertEquals("SAVED", vm.uiState.value.step.name)
        assertTrue(!vm.uiState.value.isSaving)
    }

    @Test
    fun networkError_showsSubmitError() = runTest {
        val vm = RaceGoalViewModel(RaceGoalRepository(FailingApi()))
        vm.updateForm(
            vm.uiState.value.form.copy(
                raceDistanceType = "HALF_MARATHON",
                raceDate = "2099-12-30",
                goalStyle = "FINISH"
            )
        )
        vm.continueToReview()

        vm.saveGoal()

        assertTrue(vm.uiState.value.submitError != null)
    }

    private class FakeApi : RaceGoalApiService {
        override suspend fun createRaceGoal(request: CreateRaceGoalRequest): CreateRaceGoalResponse =
            CreateRaceGoalResponse("goal-1", "ACTIVE")

        override suspend fun getCurrentRaceGoal(): CurrentRaceGoalResponse {
            throw HttpException(Response.error<CurrentRaceGoalResponse>(404, "".toResponseBody(null)))
        }
    }

    private class FailingApi : RaceGoalApiService by FakeApi() {
        override suspend fun createRaceGoal(request: CreateRaceGoalRequest): CreateRaceGoalResponse {
            throw IllegalStateException("network unavailable")
        }
    }

    private class TooSoonApi : RaceGoalApiService by FakeApi() {
        override suspend fun createRaceGoal(request: CreateRaceGoalRequest): CreateRaceGoalResponse {
            throw HttpException(
                Response.error<CreateRaceGoalResponse>(
                    400,
                    """
                    {
                      "error": {
                        "code": "VALIDATION_ERROR",
                        "message": "Race date must be at least 12 weeks away for marathon plans.",
                        "details": [
                          {"field": "raceDate", "issue": "too_soon"}
                        ]
                      }
                    }
                    """.trimIndent().toResponseBody("application/json".toMediaType())
                )
            )
        }
    }

    private class ActiveGoalConflictApi : RaceGoalApiService by FakeApi() {
        override suspend fun createRaceGoal(request: CreateRaceGoalRequest): CreateRaceGoalResponse {
            throw HttpException(
                Response.error<CreateRaceGoalResponse>(
                    409,
                    """
                    {
                      "error": {
                        "code": "CONFLICT",
                        "message": "Active goal already exists",
                        "details": [
                          {"field": "raceGoal", "issue": "active_goal_exists"}
                        ]
                      }
                    }
                    """.trimIndent().toResponseBody("application/json".toMediaType())
                )
            )
        }
    }
}
