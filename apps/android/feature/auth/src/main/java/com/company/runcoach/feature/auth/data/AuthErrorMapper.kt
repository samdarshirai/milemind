package com.company.runcoach.feature.auth.data

import com.company.runcoach.feature.auth.data.remote.ApiErrorEnvelope
import com.company.runcoach.feature.auth.domain.AuthFailure
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

class AuthErrorMapper(
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    fun map(throwable: Throwable): AuthFailure {
        if (throwable is IOException) return AuthFailure.Connectivity("Network error. Check connection and try again.")
        if (throwable is HttpException) {
            val payload = throwable.response()?.errorBody()?.string()?.let {
                runCatching { json.decodeFromString<ApiErrorEnvelope>(it) }.getOrNull()
            }?.error

            val message = payload?.message ?: "Request failed."
            val field = payload?.details?.firstOrNull()?.field

            return when {
                throwable.code() == 401 -> AuthFailure.Unauthorized(message)
                throwable.code() == 400 || throwable.code() == 422 || payload?.code == "VALIDATION_ERROR" -> {
                    AuthFailure.Validation(message = message, field = field)
                }
                throwable.code() >= 500 -> AuthFailure.Server("Server unavailable. Try again shortly.")
                else -> AuthFailure.Unknown(message)
            }
        }
        return AuthFailure.Unknown("Something went wrong. Try again.")
    }
}
