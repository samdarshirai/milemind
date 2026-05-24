package com.company.runcoach.feature.checkin.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class FatigueSignalRequest(
    val signalDate: String,
    val sleepScore: Int,
    val stressScore: Int,
    val sorenessScore: Int,
    val motivationScore: Int,
    val illnessFlag: Boolean,
    val tooBusyFlag: Boolean,
    val travellingFlag: Boolean,
    val notes: String? = null
)

@Serializable
data class FatigueSignalResponse(
    val fatigueSignalId: String,
    val readinessState: String
)

@Serializable
data class InjuryFeedbackRequest(
    val reportedAt: String,
    val hasPain: Boolean? = null,
    val bodyRegion: String? = null,
    val painType: String? = null,
    val severity: Int? = null,
    val onsetContext: String? = null,
    val canRun: Boolean? = null,
    val freeText: String? = null
)

@Serializable
data class InjuryFeedbackResponse(
    val injuryFeedbackId: String,
    val readinessState: String
)

@Serializable
data class RunnerProfileResponse(
    val timezone: String
)

@Serializable
data class ApiFieldDetail(
    val field: String? = null,
    val issue: String? = null
)

@Serializable
data class ApiErrorPayload(
    val code: String,
    val message: String,
    val details: List<ApiFieldDetail> = emptyList(),
    val correlationId: String? = null
)

@Serializable
data class ApiErrorEnvelope(
    val error: ApiErrorPayload
)
