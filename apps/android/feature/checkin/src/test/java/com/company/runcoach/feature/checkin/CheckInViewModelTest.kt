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
import com.company.runcoach.feature.checkin.ui.CheckInViewModel
import com.company.runcoach.feature.checkin.ui.model.FatigueFormState
import com.company.runcoach.feature.checkin.ui.model.PainFormState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckInViewModelTest {

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
    fun fatigueFormValidation_requiresScores() = runTest {
        val vm = CheckInViewModel(CheckInRepository(FakeApi()))

        vm.submitFatigue(onSuccess = {})

        assertTrue(vm.fatigueState.value.fieldErrors.containsKey("energyLevel"))
        assertTrue(vm.fatigueState.value.fieldErrors.containsKey("sleepQuality"))
    }

    @Test
    fun fatigueSubmit_success() = runTest {
        val vm = CheckInViewModel(CheckInRepository(FakeApi()))
        vm.updateFatigueForm(
            FatigueFormState(
                energyLevel = 3,
                sleepQuality = 3,
                muscleSoreness = 2,
                stressLevel = 2,
                notes = "Hard week"
            )
        )

        vm.submitFatigue(onSuccess = {})
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.fatigueState.value.submitSuccess)
    }

    @Test
    fun painValidation_requiresSeverityWhenPainSelected() = runTest {
        val vm = CheckInViewModel(CheckInRepository(FakeApi()))
        vm.updatePainForm(PainFormState(hasPain = true, bodyRegion = "LEFT_CALF", painType = "SHARP", onsetContext = "DURING_RUN", severity = null))

        vm.submitPain(onSuccess = {})

        assertEquals("Select pain severity.", vm.painState.value.fieldErrors["severity"])
    }

    @Test
    fun painValidation_requiresAllPainFieldsWhenPainSelected() = runTest {
        val vm = CheckInViewModel(CheckInRepository(FakeApi()))
        vm.updatePainForm(PainFormState(hasPain = true))

        vm.submitPain(onSuccess = {})

        assertEquals("Select pain location.", vm.painState.value.fieldErrors["bodyRegion"])
        assertEquals("Select pain type.", vm.painState.value.fieldErrors["painType"])
        assertEquals("Select pain severity.", vm.painState.value.fieldErrors["severity"])
        assertEquals("Select when pain started.", vm.painState.value.fieldErrors["onsetContext"])
    }

    @Test
    fun painSubmit_success() = runTest {
        val vm = CheckInViewModel(CheckInRepository(FakeApi()))
        vm.updatePainForm(PainFormState(hasPain = false, notes = "No pain"))

        vm.submitPain(onSuccess = {})
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.painState.value.submitSuccess)
        assertEquals("READY", vm.painState.value.readinessState)
    }

    @Test
    fun painSubmit_allowsZeroSeverity() = runTest {
        val vm = CheckInViewModel(CheckInRepository(FakeApi()))
        vm.updatePainForm(
            PainFormState(
                hasPain = true,
                bodyRegion = "LEFT_CALF",
                painType = "SHARP",
                severity = 0,
                onsetContext = "DURING_RUN",
                canRun = true
            )
        )

        vm.submitPain(onSuccess = {})
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.painState.value.submitSuccess)
    }

    private class FakeApi : CheckInApiService {
        override suspend fun getProfile(): RunnerProfileResponse {
            return RunnerProfileResponse(timezone = "Europe/Berlin")
        }

        override suspend fun submitFatigueSignal(request: FatigueSignalRequest): FatigueSignalResponse {
            return FatigueSignalResponse(fatigueSignalId = "fatigue-id", readinessState = "CAUTION")
        }

        override suspend fun submitInjuryFeedback(request: InjuryFeedbackRequest): InjuryFeedbackResponse {
            return InjuryFeedbackResponse(injuryFeedbackId = "pain-id", readinessState = "READY")
        }
    }
}
