package com.company.runcoach.feature.checkin

import com.company.runcoach.feature.checkin.data.CheckInRepository
import com.company.runcoach.feature.checkin.data.FatigueInput
import com.company.runcoach.feature.checkin.data.PainInput
import com.company.runcoach.feature.checkin.data.remote.CheckInApiService
import com.company.runcoach.feature.checkin.data.remote.FatigueSignalRequest
import com.company.runcoach.feature.checkin.data.remote.FatigueSignalResponse
import com.company.runcoach.feature.checkin.data.remote.InjuryFeedbackRequest
import com.company.runcoach.feature.checkin.data.remote.InjuryFeedbackResponse
import com.company.runcoach.feature.checkin.data.remote.RunnerProfileResponse
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

class CheckInRepositoryTest {

    @Test
    fun submitPain_noPainSendsMinimalPayload() = runTest {
        val api = CapturingApi()
        val repository = CheckInRepository(
            apiService = api,
            clock = Clock.fixed(Instant.parse("2026-06-15T08:00:00Z"), ZoneOffset.UTC)
        )

        repository.submitPain(
            PainInput(
                hasPain = false,
                bodyRegion = null,
                painType = null,
                severity = null,
                onsetContext = null,
                canRun = true,
                notes = ""
            )
        )

        assertNull(api.lastInjuryRequest?.bodyRegion)
        assertEquals(false, api.lastInjuryRequest?.hasPain)
        assertNull(api.lastInjuryRequest?.painType)
        assertNull(api.lastInjuryRequest?.severity)
        assertNull(api.lastInjuryRequest?.onsetContext)
        assertEquals(true, api.lastInjuryRequest?.canRun)
    }

    @Test
    fun submitPain_withPainSendsDetailedPayload() = runTest {
        val api = CapturingApi()
        val repository = CheckInRepository(
            apiService = api,
            clock = Clock.fixed(Instant.parse("2026-06-15T08:00:00Z"), ZoneOffset.UTC)
        )

        repository.submitPain(
            PainInput(
                hasPain = true,
                bodyRegion = "LEFT_CALF",
                painType = "SHARP",
                severity = 7,
                onsetContext = "DURING_RUN",
                canRun = false,
                notes = "Sharp pain"
            )
        )

        assertEquals("LEFT_CALF", api.lastInjuryRequest?.bodyRegion)
        assertEquals(true, api.lastInjuryRequest?.hasPain)
        assertEquals("SHARP", api.lastInjuryRequest?.painType)
        assertEquals(7, api.lastInjuryRequest?.severity)
        assertEquals("DURING_RUN", api.lastInjuryRequest?.onsetContext)
        assertEquals(false, api.lastInjuryRequest?.canRun)
        assertEquals("Sharp pain", api.lastInjuryRequest?.freeText)
    }

    @Test
    fun submitFatigue_usesRunnerTimezoneCalendarDay() = runTest {
        val api = CapturingApi()
        val berlinClock = Clock.fixed(
            Instant.parse("2026-06-14T22:30:00Z"),
            ZoneId.of("Europe/Berlin")
        )
        val repository = CheckInRepository(apiService = api, clock = berlinClock)
        api.profileTimezone = "America/Los_Angeles"

        repository.submitFatigue(
            FatigueInput(
                energyLevel = 3,
                sleepQuality = 3,
                muscleSoreness = 2,
                stressLevel = 2,
                illnessFlag = false,
                tooBusyFlag = false,
                travellingFlag = false,
                notes = ""
            )
        )

        assertEquals("2026-06-14", api.lastFatigueRequest?.signalDate)
    }

    @Test
    fun submitFatigue_fallsBackToCachedRunnerTimezoneIfProfileUnavailable() = runTest {
        val api = CapturingApi()
        val berlinClock = Clock.fixed(
            Instant.parse("2026-06-14T22:30:00Z"),
            ZoneId.of("Europe/Berlin")
        )
        val repository = CheckInRepository(apiService = api, clock = berlinClock)
        api.profileTimezone = "America/Los_Angeles"

        repository.submitFatigue(
            FatigueInput(
                energyLevel = 3,
                sleepQuality = 3,
                muscleSoreness = 2,
                stressLevel = 2,
                illnessFlag = false,
                tooBusyFlag = false,
                travellingFlag = false,
                notes = ""
            )
        )
        assertEquals("2026-06-14", api.lastFatigueRequest?.signalDate)

        api.failProfileLookup = true
        repository.submitFatigue(
            FatigueInput(
                energyLevel = 3,
                sleepQuality = 3,
                muscleSoreness = 2,
                stressLevel = 2,
                illnessFlag = false,
                tooBusyFlag = false,
                travellingFlag = false,
                notes = ""
            )
        )

        assertEquals("2026-06-14", api.lastFatigueRequest?.signalDate)
    }

    @Test
    fun submitFatigue_mapsBackendScoreFieldErrorsToUiMessages() = runTest {
        val api = CapturingApi()
        api.fatigueHttpException = HttpException(
            Response.error<FatigueSignalResponse>(
                400,
                """
                {
                  "error": {
                    "code": "VALIDATION_ERROR",
                    "message": "Validation failed.",
                    "details": [
                      {"field":"sleepScore","issue":"required"},
                      {"field":"stressScore","issue":"required"},
                      {"field":"sorenessScore","issue":"required"},
                      {"field":"motivationScore","issue":"required"}
                    ]
                  }
                }
                """.trimIndent().toResponseBody("application/json".toMediaType())
            )
        )
        val repository = CheckInRepository(apiService = api)

        val result = repository.submitFatigue(
            FatigueInput(
                energyLevel = 3,
                sleepQuality = 3,
                muscleSoreness = 2,
                stressLevel = 2,
                illnessFlag = false,
                tooBusyFlag = false,
                travellingFlag = false,
                notes = ""
            )
        )

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as com.company.runcoach.feature.checkin.data.CheckInSubmitException
        assertEquals("Select your sleep quality.", error.fieldErrors["sleepScore"])
        assertEquals("Select your stress level.", error.fieldErrors["stressScore"])
        assertEquals("Select your muscle soreness.", error.fieldErrors["sorenessScore"])
        assertEquals("Select your energy level.", error.fieldErrors["motivationScore"])
    }

    @Test
    fun submitFatigue_usesUtcFallbackWhenProfileUnavailableAndNoCachedTimezone() = runTest {
        val api = CapturingApi()
        api.failProfileLookup = true
        val berlinClock = Clock.fixed(
            Instant.parse("2026-06-14T22:30:00Z"),
            ZoneId.of("Europe/Berlin")
        )
        val repository = CheckInRepository(apiService = api, clock = berlinClock)

        repository.submitFatigue(
            FatigueInput(
                energyLevel = 3,
                sleepQuality = 3,
                muscleSoreness = 2,
                stressLevel = 2,
                illnessFlag = false,
                tooBusyFlag = false,
                travellingFlag = false,
                notes = ""
            )
        )

        assertEquals("2026-06-14", api.lastFatigueRequest?.signalDate)
    }

    @Test
    fun submitPain_usesUtcTimestampWithNonUtcClock() = runTest {
        val api = CapturingApi()
        val tokyoClock = Clock.fixed(
            Instant.parse("2026-06-14T23:30:00Z"),
            ZoneId.of("Asia/Tokyo")
        )
        val repository = CheckInRepository(apiService = api, clock = tokyoClock)

        repository.submitPain(
            PainInput(
                hasPain = false,
                bodyRegion = null,
                painType = null,
                severity = null,
                onsetContext = null,
                canRun = true,
                notes = ""
            )
        )

        assertEquals("2026-06-14T23:30:00Z", api.lastInjuryRequest?.reportedAt)
    }

    private class CapturingApi : CheckInApiService {
        var lastFatigueRequest: FatigueSignalRequest? = null
        var lastInjuryRequest: InjuryFeedbackRequest? = null
        var profileTimezone: String = "Europe/Berlin"
        var failProfileLookup: Boolean = false
        var fatigueHttpException: HttpException? = null

        override suspend fun getProfile(): RunnerProfileResponse {
            if (failProfileLookup) {
                error("profile unavailable")
            }
            return RunnerProfileResponse(timezone = profileTimezone)
        }

        override suspend fun submitFatigueSignal(request: FatigueSignalRequest): FatigueSignalResponse {
            lastFatigueRequest = request
            fatigueHttpException?.let { throw it }
            return FatigueSignalResponse("fatigue-id", "READY")
        }

        override suspend fun submitInjuryFeedback(request: InjuryFeedbackRequest): InjuryFeedbackResponse {
            lastInjuryRequest = request
            return InjuryFeedbackResponse("injury-id", "READY")
        }
    }
}
