package com.company.runcoach.feature.checkin.ui.model

data class FatigueFormState(
    val energyLevel: Int? = null,
    val sleepQuality: Int? = null,
    val muscleSoreness: Int? = null,
    val stressLevel: Int? = null,
    val illnessFlag: Boolean = false,
    val tooBusyFlag: Boolean = false,
    val travellingFlag: Boolean = false,
    val notes: String = ""
)

data class PainFormState(
    val hasPain: Boolean? = null,
    val bodyRegion: String? = null,
    val painType: String? = null,
    val severity: Int? = null,
    val onsetContext: String? = null,
    val canRun: Boolean? = null,
    val notes: String = ""
)

data class FatigueUiState(
    val form: FatigueFormState = FatigueFormState(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val submitSuccess: Boolean = false
)

data class PainUiState(
    val form: PainFormState = PainFormState(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val submitSuccess: Boolean = false,
    val readinessState: String? = null
)
