package com.company.runcoach.feature.auth.data

import com.company.runcoach.core.datastore.session.SessionStore
import com.company.runcoach.feature.auth.data.remote.AuthApiService
import com.company.runcoach.feature.auth.data.remote.LoginRequest
import com.company.runcoach.feature.auth.data.remote.RefreshRequest
import com.company.runcoach.feature.auth.data.remote.RegisterRequest
import com.company.runcoach.feature.auth.domain.AuthFailure
import com.company.runcoach.feature.auth.domain.AuthSession
import com.company.runcoach.feature.onboarding.data.OnboardingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionStore: SessionStore,
    private val errorMapper: AuthErrorMapper,
    private val onboardingRepository: OnboardingRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun signIn(email: String, password: String): Result<AuthSession> = withContext(ioDispatcher) {
        runCatching {
            val response = authApiService.login(LoginRequest(email.trim(), password))
            sessionStore.save(response.accessToken, response.refreshToken)
            AuthSession(onboardingRequired = response.onboardingRequired)
        }.mapFailure()
    }

    suspend fun signUp(email: String, password: String): Result<AuthSession> = withContext(ioDispatcher) {
        runCatching {
            val response = authApiService.register(
                RegisterRequest(
                    email = email.trim(),
                    password = password,
                    timezone = ZoneId.systemDefault().id
                )
            )
            sessionStore.save(response.accessToken, response.refreshToken)
            AuthSession(onboardingRequired = response.onboardingRequired)
        }.mapFailure()
    }

    suspend fun restoreSession(): Result<AuthSession> = withContext(ioDispatcher) {
        val refreshToken = sessionStore.refreshTokenOrNull()
            ?: return@withContext Result.failure(AuthFailure.Unauthorized("No saved session."))

        val result = runCatching {
            val response = authApiService.refresh(RefreshRequest(refreshToken))
            sessionStore.save(response.accessToken, response.refreshToken)
            // Session restore should not fail if profile/onboarding lookup is transiently unavailable.
            val needsOnboarding = onboardingRepository.isOnboardingComplete()
                .map { complete -> !complete }
                .getOrDefault(false)
            AuthSession(onboardingRequired = needsOnboarding)
        }.mapFailure()

        if (result.exceptionOrNull() is AuthFailure.Unauthorized) {
            sessionStore.clear()
        }
        result
    }

    fun clearSession() {
        sessionStore.clear()
    }

    private fun <T> Result<T>.mapFailure(): Result<T> = fold(
        onSuccess = { Result.success(it) },
        onFailure = {
            if (it is AuthFailure) Result.failure(it) else Result.failure(errorMapper.map(it))
        }
    )
}
