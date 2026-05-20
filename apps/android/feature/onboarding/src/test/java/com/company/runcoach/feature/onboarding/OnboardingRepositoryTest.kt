package com.company.runcoach.feature.onboarding

import com.company.runcoach.feature.onboarding.data.OnboardingRepository
import com.company.runcoach.feature.onboarding.data.remote.OnboardingApiService
import com.company.runcoach.feature.onboarding.data.remote.OnboardingRequest
import com.company.runcoach.feature.onboarding.data.remote.OnboardingResponse
import com.company.runcoach.feature.onboarding.data.remote.ProfileData
import com.company.runcoach.feature.onboarding.data.remote.ProfileResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingRepositoryTest {

    @Test
    fun isOnboardingComplete_trueWhenInjuryHistoryIsMissing() = runTest {
        val repository = OnboardingRepository(MissingInjuryHistoryApi())

        val result = repository.isOnboardingComplete()

        assertEquals(true, result.getOrThrow())
    }

    @Test
    fun isOnboardingComplete_trueWhenInjuryHistoryIsPartial() = runTest {
        val repository = OnboardingRepository(PartialInjuryHistoryApi())

        val result = repository.isOnboardingComplete()

        assertEquals(true, result.getOrThrow())
    }

    @Test
    fun isOnboardingComplete_trueWhenInjuryHistoryContainsNullValue() = runTest {
        val repository = OnboardingRepository(NullInjurySummaryApi())

        val result = repository.isOnboardingComplete()

        assertEquals(true, result.getOrThrow())
    }

    @Test
    fun submitOnboarding_mapsValidationIssueToHumanReadableMessage() = runTest {
        val repository = OnboardingRepository(ValidationErrorApi())

        val result = repository.submitOnboarding(
            com.company.runcoach.feature.onboarding.data.OnboardingInput(
                birthYear = 1990,
                sex = "FEMALE",
                experienceLevel = "BEGINNER",
                typicalWeeklyDistanceKm = 20.0,
                longestRecentRunKm = 10.0,
                hadRunningInjuryLast12Months = true,
                injuryHistory = "",
                preferredRunDays = listOf("MONDAY", "WEDNESDAY", "SATURDAY"),
                preferredLongRunDay = "SATURDAY",
                strengthDaysPerWeek = 1,
                units = "KM",
                timezone = "Europe/Berlin"
            )
        )

        assertTrue(result.isFailure)
        val err = result.exceptionOrNull() as com.company.runcoach.feature.onboarding.data.OnboardingSubmissionException
        assertEquals(
            "Choose yes or no for recent injury history.",
            err.fieldErrors["injuryHistory.hadRunningInjuryLast12Months"]
        )
    }
}

private class MissingInjuryHistoryApi : OnboardingApiService {
    override suspend fun submitOnboarding(request: OnboardingRequest): OnboardingResponse = OnboardingResponse("u", "p")

    override suspend fun getProfile(): ProfileResponse = baseProfile(injuryHistory = null)
}

private class PartialInjuryHistoryApi : OnboardingApiService {
    override suspend fun submitOnboarding(request: OnboardingRequest): OnboardingResponse = OnboardingResponse("u", "p")

    override suspend fun getProfile(): ProfileResponse = baseProfile(
        injuryHistory = mapOf("hadRunningInjuryLast12Months" to JsonPrimitive(false))
    )
}

private class NullInjurySummaryApi : OnboardingApiService {
    override suspend fun submitOnboarding(request: OnboardingRequest): OnboardingResponse = OnboardingResponse("u", "p")

    override suspend fun getProfile(): ProfileResponse = baseProfile(
        injuryHistory = mapOf(
            "hadRunningInjuryLast12Months" to JsonPrimitive(true),
            "summary" to JsonNull
        )
    )
}

private class ValidationErrorApi : OnboardingApiService {
    override suspend fun submitOnboarding(request: OnboardingRequest): OnboardingResponse {
        throw HttpException(
            Response.error<OnboardingResponse>(
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

    override suspend fun getProfile(): ProfileResponse = baseProfile(injuryHistory = null)
}

private fun baseProfile(
    injuryHistory: Map<String, kotlinx.serialization.json.JsonElement>?
) = ProfileResponse(
    userId = "u",
    email = "runner@example.com",
    timezone = "Europe/Berlin",
    profile = ProfileData(
        experienceLevel = "BEGINNER",
        typicalWeeklyDistanceKm = 24.0,
        longestRecentRunKm = 10.0,
        preferredRunDays = listOf("TUESDAY", "THURSDAY", "SATURDAY"),
        preferredLongRunDay = "SATURDAY",
        goalStyle = "FINISH",
        strengthDaysPerWeek = 1,
        units = "KM",
        injuryHistory = injuryHistory
    )
)
