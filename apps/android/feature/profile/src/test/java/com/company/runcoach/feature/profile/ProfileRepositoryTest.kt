package com.company.runcoach.feature.profile

import com.company.runcoach.feature.profile.data.ProfileRepository
import com.company.runcoach.feature.profile.data.ProfileSaveException
import com.company.runcoach.feature.profile.data.remote.ProfileApiService
import com.company.runcoach.feature.profile.data.remote.ProfileData
import com.company.runcoach.feature.profile.data.remote.ProfileResponse
import com.company.runcoach.feature.profile.data.remote.ProfileUpdateRequest
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
class ProfileRepositoryTest {

    @Test
    fun saveProfile_mapsValidationIssueToHumanReadableMessage() = runTest {
        val repository = ProfileRepository(ValidationErrorApi())

        val result = repository.saveProfile(
            com.company.runcoach.feature.profile.data.EditableProfile(
                weeklyDistance = "20",
                longestRun = "10",
                preferredRunDays = setOf("MONDAY", "WEDNESDAY", "SATURDAY"),
                preferredLongRunDay = "SATURDAY",
                strengthDaysPerWeek = 1,
                units = "KM",
                timezone = "Europe/Berlin",
                hadRunningInjuryLast12Months = true
            )
        )

        assertTrue(result.isFailure)
        val err = result.exceptionOrNull() as ProfileSaveException
        assertEquals(
            "Choose yes or no for recent injury history.",
            err.fieldErrors["injuryHistory.hadRunningInjuryLast12Months"]
        )
    }

    private class ValidationErrorApi : ProfileApiService {
        override suspend fun getProfile(): ProfileResponse = ProfileResponse(
            userId = "u",
            email = "runner@example.com",
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
}
