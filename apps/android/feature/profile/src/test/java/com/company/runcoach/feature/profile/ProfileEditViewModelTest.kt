package com.company.runcoach.feature.profile

import com.company.runcoach.feature.profile.data.EditableProfile
import com.company.runcoach.feature.profile.data.ProfileRepository
import com.company.runcoach.feature.profile.data.remote.InjuryHistoryUpdateRequest
import com.company.runcoach.feature.profile.data.remote.ProfileApiService
import com.company.runcoach.feature.profile.data.remote.ProfileData
import com.company.runcoach.feature.profile.data.remote.ProfileResponse
import com.company.runcoach.feature.profile.data.remote.ProfileUpdateRequest
import com.company.runcoach.feature.profile.ui.ProfileEditViewModel
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
class ProfileEditViewModelTest {

    @Test
    fun loadsExistingProfile() = runTest {
        val vm = ProfileEditViewModel(ProfileRepository(SuccessApi()))
        vm.load()
        assertEquals("20.0", vm.uiState.value.profile.weeklyDistance)
    }

    @Test
    fun saveSuccess_setsSuccessState() = runTest {
        val vm = ProfileEditViewModel(ProfileRepository(SuccessApi()))
        vm.load()
        vm.updateProfile(
            vm.uiState.value.profile.copy(
                weeklyDistance = "30",
                longestRun = "12",
                preferredRunDays = setOf("MONDAY", "WEDNESDAY", "SATURDAY")
            )
        )
        vm.save()
        assertTrue(vm.uiState.value.saveSuccess)
    }

    @Test
    fun saveError_setsErrorState() = runTest {
        val vm = ProfileEditViewModel(ProfileRepository(FailingApi()))
        vm.load()
        vm.updateProfile(
            EditableProfile(
                weeklyDistance = "20",
                longestRun = "10",
                preferredRunDays = setOf("MONDAY", "WEDNESDAY", "SATURDAY"),
                preferredLongRunDay = "SATURDAY",
                strengthDaysPerWeek = 1,
                units = "KM",
                timezone = "Europe/Berlin"
            )
        )
        vm.save()
        assertTrue(vm.uiState.value.errorMessage != null)
    }

    @Test
    fun saveValidation_preventsRequestWhenRunDaysAreTooFew() = runTest {
        val api = RecordingApi()
        val vm = ProfileEditViewModel(ProfileRepository(api))
        vm.load()
        vm.updateProfile(
            vm.uiState.value.profile.copy(
                preferredRunDays = setOf("MONDAY"),
                preferredLongRunDay = "MONDAY",
                strengthDaysPerWeek = 1,
                units = "KM",
                timezone = "Europe/Berlin"
            )
        )

        vm.save()

        assertEquals("Choose at least 3 run days.", vm.uiState.value.fieldErrors["preferredRunDays"])
        assertTrue(api.lastUpdateRequest == null)
    }

    @Test
    fun save_persistsEditableFieldsShownInProfileEdit() = runTest {
        val api = RecordingApi()
        val vm = ProfileEditViewModel(ProfileRepository(api))
        vm.load()
        vm.updateProfile(
            vm.uiState.value.profile.copy(
                preferredRunDays = setOf("MONDAY", "THURSDAY", "SUNDAY"),
                preferredLongRunDay = "SUNDAY",
                strengthDaysPerWeek = 2,
                units = "MILES",
                timezone = "America/New_York",
                hadRunningInjuryLast12Months = true,
                injuryHistorySummary = "Resolved Achilles soreness"
            )
        )

        vm.save()

        val captured = api.lastUpdateRequest
        requireNotNull(captured)
        assertEquals(listOf("MONDAY", "SUNDAY", "THURSDAY"), captured.preferredRunDays.sorted())
        assertEquals("SUNDAY", captured.preferredLongRunDay)
        assertEquals(2, captured.strengthDaysPerWeek)
        assertEquals("MILES", captured.units)
        assertEquals("America/New_York", captured.timezone)
        assertEquals("Resolved Achilles soreness", captured.injuryHistory?.summary)
        assertEquals(true, captured.injuryHistory?.hadRunningInjuryLast12Months)
    }

    @Test
    fun saveValidationError_mapsFieldErrorsFromEnvelope() = runTest {
        val vm = ProfileEditViewModel(ProfileRepository(ValidationErrorApi()))
        vm.load()
        vm.updateProfile(
            vm.uiState.value.profile.copy(
                preferredRunDays = setOf("MONDAY", "THURSDAY", "SUNDAY"),
                preferredLongRunDay = "SUNDAY",
                strengthDaysPerWeek = 2,
                units = "MILES",
                timezone = "America/New_York"
            )
        )

        vm.save()

        assertEquals(
            "Choose yes or no for recent injury history.",
            vm.uiState.value.fieldErrors["injuryHistory.hadRunningInjuryLast12Months"]
        )
        assertEquals(null, vm.uiState.value.errorMessage)
    }

    private class SuccessApi : ProfileApiService {
        override suspend fun getProfile(): ProfileResponse = ProfileResponse(
            userId = "u",
            email = "a@b.com",
            timezone = "Europe/Berlin",
            profile = ProfileData(
                experienceLevel = "BEGINNER",
                typicalWeeklyDistanceKm = 20.0,
                longestRecentRunKm = 10.0,
                preferredRunDays = listOf("MONDAY", "WEDNESDAY", "SATURDAY"),
                preferredLongRunDay = "SATURDAY",
                goalStyle = "FINISH",
                strengthDaysPerWeek = 1,
                units = "KM"
            )
        )

        override suspend fun updateProfile(request: ProfileUpdateRequest): ProfileResponse = getProfile()
    }

    private class FailingApi : ProfileApiService {
        override suspend fun getProfile(): ProfileResponse = SuccessApi().getProfile()
        override suspend fun updateProfile(request: ProfileUpdateRequest): ProfileResponse {
            throw IllegalStateException("network")
        }
    }

    private class ValidationErrorApi : ProfileApiService {
        override suspend fun getProfile(): ProfileResponse = SuccessApi().getProfile()

        override suspend fun updateProfile(request: ProfileUpdateRequest): ProfileResponse {
            throw HttpException(
                Response.error<ProfileResponse>(
                    400,
                    """
                    {
                      "error": {
                        "code": "VALIDATION_ERROR",
                        "message": "Validation failed",
                        "details": [
                          {"field": "injuryHistory.hadRunningInjuryLast12Months", "issue": "invalid_type"}
                        ]
                      }
                    }
                    """.trimIndent().toResponseBody("application/json".toMediaType())
                )
            )
        }
    }

    private class RecordingApi : ProfileApiService {
        var lastUpdateRequest: ProfileUpdateRequest? = null

        override suspend fun getProfile(): ProfileResponse = SuccessApi().getProfile()

        override suspend fun updateProfile(request: ProfileUpdateRequest): ProfileResponse {
            lastUpdateRequest = request
            return getProfile()
        }
    }
}
