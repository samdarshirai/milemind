package com.company.runcoach.feature.onboarding

import com.company.runcoach.feature.onboarding.data.OnboardingRepository
import com.company.runcoach.feature.onboarding.data.remote.OnboardingApiService
import com.company.runcoach.feature.onboarding.data.remote.OnboardingRequest
import com.company.runcoach.feature.onboarding.data.remote.ProfileData
import com.company.runcoach.feature.onboarding.data.remote.ProfileResponse
import com.company.runcoach.feature.onboarding.ui.OnboardingViewModel
import com.company.runcoach.feature.onboarding.ui.model.AvailabilityForm
import com.company.runcoach.feature.onboarding.ui.model.RunningHistoryForm
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
class OnboardingViewModelTest {

    @Test
    fun runningHistory_validationShowsErrors() = runTest {
        val vm = OnboardingViewModel(OnboardingRepository(FakeApi()))
        vm.nextFromIntro()
        vm.updateRunningHistory(RunningHistoryForm(birthYear = "2015", weeklyDistance = "0", longestRun = "-1"))

        vm.nextFromRunningHistory()

        val errors = vm.uiState.value.fieldErrors
        assertTrue(errors.containsKey("birthYear"))
        assertTrue(errors.containsKey("weeklyDistance"))
        assertTrue(errors.containsKey("longestRun"))
        assertTrue(errors.containsKey("sex"))
        assertTrue(errors.containsKey("experienceLevel"))
    }

    @Test
    fun availability_validationShowsError_whenRunDaysBelowMinimum() = runTest {
        val vm = OnboardingViewModel(OnboardingRepository(FakeApi()))
        vm.nextFromIntro()
        vm.updateRunningHistory(
            RunningHistoryForm(
                birthYear = "1990",
                sex = "FEMALE",
                experienceLevel = "BEGINNER",
                weeklyDistance = "10",
                longestRun = "5"
            )
        )
        vm.nextFromRunningHistory()
        vm.updateAvailability(AvailabilityForm(preferredRunDays = setOf("MONDAY"), strengthDaysPerWeek = 1, units = "KM", timezone = "Europe/Berlin"))

        vm.submit()

        assertEquals("Choose at least 3 run days.", vm.uiState.value.fieldErrors["preferredRunDays"])
        assertEquals("Choose a long run day.", vm.uiState.value.fieldErrors["preferredLongRunDay"])
    }

    @Test
    fun successfulSubmission_setsSuccessState() = runTest {
        val api = CapturingApi()
        val vm = OnboardingViewModel(OnboardingRepository(api))
        vm.nextFromIntro()
        vm.updateRunningHistory(
            RunningHistoryForm(
                birthYear = "1990",
                sex = "PREFER_NOT_TO_SAY",
                experienceLevel = "INTERMEDIATE",
                weeklyDistance = "30",
                longestRun = "12"
            )
        )
        vm.nextFromRunningHistory()
        vm.updateAvailability(
            AvailabilityForm(
                preferredRunDays = setOf("MONDAY", "WEDNESDAY", "SATURDAY"),
                preferredLongRunDay = "SATURDAY",
                strengthDaysPerWeek = 1,
                units = "KM",
                timezone = "Europe/Berlin"
            )
        )
        vm.submit()

        assertTrue(vm.uiState.value.isSuccess)
        assertTrue(vm.uiState.value.fieldErrors.isEmpty())
        assertEquals("PREFER_NOT_TO_SAY", api.lastRequest?.profile?.sex)
        assertEquals("INTERMEDIATE", api.lastRequest?.profile?.experienceLevel)
        assertEquals("SATURDAY", api.lastRequest?.profile?.preferredLongRunDay)
        assertEquals("Europe/Berlin", api.lastRequest?.profile?.timezone)
        assertEquals(null, api.lastRequest?.profile?.injuryHistory?.hadRunningInjuryLast12Months)
    }

    @Test
    fun apiFailure_setsSubmitError() = runTest {
        val vm = OnboardingViewModel(OnboardingRepository(FailingApi()))
        vm.nextFromIntro()
        vm.updateRunningHistory(
            RunningHistoryForm(
                birthYear = "1990",
                sex = "FEMALE",
                experienceLevel = "BEGINNER",
                weeklyDistance = "20",
                longestRun = "8"
            )
        )
        vm.nextFromRunningHistory()
        vm.updateAvailability(
            AvailabilityForm(
                preferredRunDays = setOf("MONDAY", "THURSDAY", "SATURDAY"),
                preferredLongRunDay = "SATURDAY",
                strengthDaysPerWeek = 1,
                units = "KM",
                timezone = "Europe/Berlin"
            )
        )

        vm.submit()

        assertTrue(vm.uiState.value.submitError != null)
    }

    @Test
    fun apiValidationFailure_mapsFieldErrorsFromEnvelope() = runTest {
        val vm = OnboardingViewModel(OnboardingRepository(ValidationErrorApi()))
        vm.nextFromIntro()
        vm.updateRunningHistory(
            RunningHistoryForm(
                birthYear = "1990",
                sex = "FEMALE",
                experienceLevel = "BEGINNER",
                weeklyDistance = "20",
                longestRun = "8",
                hadRunningInjuryLast12Months = true
            )
        )
        vm.nextFromRunningHistory()
        vm.updateAvailability(
            AvailabilityForm(
                preferredRunDays = setOf("MONDAY", "THURSDAY", "SATURDAY"),
                preferredLongRunDay = "SATURDAY",
                strengthDaysPerWeek = 1,
                units = "KM",
                timezone = "Europe/Berlin"
            )
        )

        vm.submit()

        assertEquals(
            "Choose yes or no for recent injury history.",
            vm.uiState.value.fieldErrors["injuryHistory.hadRunningInjuryLast12Months"]
        )
        assertEquals(null, vm.uiState.value.submitError)
    }

    @Test
    fun availability_validationShowsTimezoneError_whenTimezoneInvalid() = runTest {
        val vm = OnboardingViewModel(OnboardingRepository(FakeApi()))
        vm.nextFromIntro()
        vm.updateRunningHistory(
            RunningHistoryForm(
                birthYear = "1990",
                sex = "FEMALE",
                experienceLevel = "BEGINNER",
                weeklyDistance = "10",
                longestRun = "5"
            )
        )
        vm.nextFromRunningHistory()
        vm.updateAvailability(
            AvailabilityForm(
                preferredRunDays = setOf("MONDAY", "WEDNESDAY", "SATURDAY"),
                preferredLongRunDay = "SATURDAY",
                strengthDaysPerWeek = 1,
                units = "KM",
                timezone = "Invalid/Zone"
            )
        )

        vm.submit()

        assertEquals("Enter a valid timezone (for example, Europe/Berlin).", vm.uiState.value.fieldErrors["timezone"])
    }

    @Test
    fun availability_validationShowsError_whenLongRunDayOutsideRunDays() = runTest {
        val vm = OnboardingViewModel(OnboardingRepository(FakeApi()))
        vm.nextFromIntro()
        vm.updateRunningHistory(
            RunningHistoryForm(
                birthYear = "1990",
                sex = "FEMALE",
                experienceLevel = "BEGINNER",
                weeklyDistance = "10",
                longestRun = "5"
            )
        )
        vm.nextFromRunningHistory()
        vm.updateAvailability(
            AvailabilityForm(
                preferredRunDays = setOf("MONDAY", "WEDNESDAY", "SATURDAY"),
                preferredLongRunDay = "SUNDAY",
                strengthDaysPerWeek = 1,
                units = "KM",
                timezone = "Europe/Berlin"
            )
        )

        vm.submit()

        assertEquals(
            "Long run day must be one of your selected run days.",
            vm.uiState.value.fieldErrors["preferredLongRunDay"]
        )
    }

    private class FakeApi : OnboardingApiService {
        override suspend fun submitOnboarding(request: OnboardingRequest) = com.company.runcoach.feature.onboarding.data.remote.OnboardingResponse("u", "p")
        override suspend fun getProfile(): ProfileResponse = ProfileResponse("u", "a@b.com", "Europe/Berlin", ProfileData(experienceLevel = "BEGINNER", typicalWeeklyDistanceKm = 20.0, longestRecentRunKm = 10.0, preferredRunDays = listOf("MONDAY"), preferredLongRunDay = "SUNDAY", goalStyle = "FINISH", strengthDaysPerWeek = 1, units = "KM"))
    }

    private class FailingApi : OnboardingApiService {
        override suspend fun submitOnboarding(request: OnboardingRequest): com.company.runcoach.feature.onboarding.data.remote.OnboardingResponse {
            throw IllegalStateException("validation failed")
        }

        override suspend fun getProfile(): ProfileResponse {
            throw IllegalStateException("missing")
        }
    }

    private class ValidationErrorApi : OnboardingApiService {
        override suspend fun submitOnboarding(request: OnboardingRequest): com.company.runcoach.feature.onboarding.data.remote.OnboardingResponse {
            throw HttpException(
                Response.error<com.company.runcoach.feature.onboarding.data.remote.OnboardingResponse>(
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

        override suspend fun getProfile(): ProfileResponse = FakeApi().getProfile()
    }

    private class CapturingApi : OnboardingApiService {
        var lastRequest: OnboardingRequest? = null
        override suspend fun submitOnboarding(request: OnboardingRequest): com.company.runcoach.feature.onboarding.data.remote.OnboardingResponse {
            lastRequest = request
            return com.company.runcoach.feature.onboarding.data.remote.OnboardingResponse("u", "p")
        }

        override suspend fun getProfile(): ProfileResponse = ProfileResponse(
            "u",
            "a@b.com",
            "Europe/Berlin",
            ProfileData(
                experienceLevel = "BEGINNER",
                typicalWeeklyDistanceKm = 20.0,
                longestRecentRunKm = 10.0,
                preferredRunDays = listOf("MONDAY"),
                preferredLongRunDay = "SUNDAY",
                goalStyle = "FINISH",
                strengthDaysPerWeek = 1,
                units = "KM"
            )
        )
    }
}
