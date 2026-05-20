package com.company.runcoach.feature.auth.ui

import com.company.runcoach.core.datastore.session.SessionStore
import com.company.runcoach.core.datastore.session.TokenStorage
import com.company.runcoach.feature.auth.data.AuthErrorMapper
import com.company.runcoach.feature.auth.data.AuthRepository
import com.company.runcoach.feature.auth.data.remote.AuthApiService
import com.company.runcoach.feature.auth.data.remote.AuthResponse
import com.company.runcoach.feature.auth.data.remote.LoginRequest
import com.company.runcoach.feature.auth.data.remote.RefreshRequest
import com.company.runcoach.feature.auth.data.remote.RefreshResponse
import com.company.runcoach.feature.auth.data.remote.RegisterRequest
import com.company.runcoach.feature.auth.domain.AuthFailure
import com.company.runcoach.feature.auth.ui.model.SplashDestination
import com.company.runcoach.feature.onboarding.data.OnboardingRepository
import com.company.runcoach.feature.onboarding.data.remote.OnboardingApiService
import com.company.runcoach.feature.onboarding.data.remote.OnboardingRequest
import com.company.runcoach.feature.onboarding.data.remote.OnboardingResponse
import com.company.runcoach.feature.onboarding.data.remote.ProfileData
import com.company.runcoach.feature.onboarding.data.remote.ProfileResponse
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

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
    fun signInViewModel_successState() = runTest(dispatcher) {
        val repository = repositoryWithResponse(login = AuthResponse("a", "r", onboardingRequired = false))
        val viewModel = SignInViewModel(repository)

        viewModel.onEmailChanged("runner@example.com")
        viewModel.onPasswordChanged("StrongPass1!")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertEquals(false, viewModel.uiState.value.onboardingRequired)
    }

    @Test
    fun signInViewModel_validationErrorState() = runTest(dispatcher) {
        val repository = repositoryWithResponse(login = AuthResponse("a", "r"))
        val viewModel = SignInViewModel(repository)

        viewModel.onEmailChanged("bad")
        viewModel.onPasswordChanged("short")
        viewModel.submit()

        assertEquals("Enter a valid email", viewModel.uiState.value.emailError)
        assertEquals("Password must be at least 10 characters", viewModel.uiState.value.passwordError)
    }

    @Test
    fun signUpViewModel_successState() = runTest(dispatcher) {
        val repository = repositoryWithResponse(register = AuthResponse("a", "r", onboardingRequired = true))
        val viewModel = SignUpViewModel(repository)

        viewModel.onEmailChanged("runner@example.com")
        viewModel.onPasswordChanged("StrongPass1!")
        viewModel.onConfirmPasswordChanged("StrongPass1!")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertEquals(true, viewModel.uiState.value.onboardingRequired)
    }

    @Test
    fun signUpViewModel_validationErrorState() = runTest(dispatcher) {
        val repository = repositoryWithResponse(register = AuthResponse("a", "r"))
        val viewModel = SignUpViewModel(repository)

        viewModel.onEmailChanged("bad")
        viewModel.onPasswordChanged("short")
        viewModel.onConfirmPasswordChanged("different")
        viewModel.submit()

        assertEquals("Enter a valid email", viewModel.uiState.value.emailError)
        assertEquals("Password must be at least 10 characters", viewModel.uiState.value.passwordError)
        assertEquals("Passwords do not match", viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun sessionRestore_validStoredToken() = runTest(dispatcher) {
        val repository = repositoryWithResponse(refresh = RefreshResponse("newA", "newR"), initialRefresh = "oldR")

        val result = repository.restoreSession()

        assertTrue(result.isSuccess)
    }

    @Test
    fun sessionRestore_succeedsWhenProfileContainsBooleanInjuryFlag() = runTest(dispatcher) {
        val repository = repositoryWithResponse(
            refresh = RefreshResponse("newA", "newR"),
            initialRefresh = "oldR",
            onboardingApi = BooleanInjuryOnboardingApi()
        )

        val result = repository.restoreSession()

        assertTrue(result.isSuccess)
        assertEquals(false, result.getOrThrow().onboardingRequired)
    }

    @Test
    fun splashViewModel_routesToMainOnSessionRestoreSuccess() = runTest(dispatcher) {
        val repository = repositoryWithResponse(refresh = RefreshResponse("newA", "newR"), initialRefresh = "oldR")
        val viewModel = SplashViewModel(repository)

        advanceUntilIdle()

        assertEquals(SplashDestination.MAIN, viewModel.uiState.value.destination)
    }

    @Test
    fun splashViewModel_routesToOnboardingWhenProfileIsMissing() = runTest(dispatcher) {
        val repository = repositoryWithResponse(
            refresh = RefreshResponse("newA", "newR"),
            initialRefresh = "oldR",
            onboardingApi = MissingProfileOnboardingApi()
        )
        val viewModel = SplashViewModel(repository)

        advanceUntilIdle()

        assertEquals(SplashDestination.ONBOARDING, viewModel.uiState.value.destination)
    }

    @Test
    fun splashViewModel_routesToSignInWhenNoStoredSession() = runTest(dispatcher) {
        val repository = repositoryWithResponse(initialRefresh = null)
        val viewModel = SplashViewModel(repository)

        advanceUntilIdle()

        assertEquals(SplashDestination.SIGN_IN, viewModel.uiState.value.destination)
    }

    @Test
    fun sessionRestore_missingOrInvalidToken() = runTest(dispatcher) {
        val missingRepository = repositoryWithResponse(refresh = RefreshResponse("newA", "newR"), initialRefresh = null)
        val missing = missingRepository.restoreSession()
        assertTrue(missing.exceptionOrNull() is AuthFailure.Unauthorized)

        val invalidRepository = repositoryWithErrorOnRefresh(initialRefresh = "bad")
        val invalid = invalidRepository.restoreSession()
        assertTrue(invalid.isFailure)
    }

    @Test
    fun sessionRestore_networkFailure_keepsStoredRefreshToken() = runTest(dispatcher) {
        val tokenStorage = InMemoryTokenStorage(refreshToken = "oldR")
        val repository = AuthRepository(
            authApiService = object : AuthApiService {
                override suspend fun register(request: RegisterRequest): AuthResponse = AuthResponse("a", "r")
                override suspend fun login(request: LoginRequest): AuthResponse = AuthResponse("a", "r")
                override suspend fun refresh(request: RefreshRequest): RefreshResponse {
                    throw IOException("network")
                }
            },
            sessionStore = SessionStore(tokenStorage),
            errorMapper = AuthErrorMapper(),
            onboardingRepository = OnboardingRepository(SuccessfulOnboardingApi()),
            ioDispatcher = dispatcher
        )

        val result = repository.restoreSession()

        assertTrue(result.exceptionOrNull() is AuthFailure.Connectivity)
        assertEquals("oldR", tokenStorage.readRefreshToken())
    }

    @Test
    fun sessionRestore_unauthorizedFailure_clearsStoredRefreshToken() = runTest(dispatcher) {
        val tokenStorage = InMemoryTokenStorage(refreshToken = "oldR")
        val repository = AuthRepository(
            authApiService = object : AuthApiService {
                override suspend fun register(request: RegisterRequest): AuthResponse = AuthResponse("a", "r")
                override suspend fun login(request: LoginRequest): AuthResponse = AuthResponse("a", "r")
                override suspend fun refresh(request: RefreshRequest): RefreshResponse {
                    throw AuthFailure.Unauthorized("expired")
                }
            },
            sessionStore = SessionStore(tokenStorage),
            errorMapper = AuthErrorMapper(),
            onboardingRepository = OnboardingRepository(SuccessfulOnboardingApi()),
            ioDispatcher = dispatcher
        )

        val result = repository.restoreSession()

        assertTrue(result.exceptionOrNull() is AuthFailure.Unauthorized)
        assertEquals(null, tokenStorage.readRefreshToken())
    }

    @Test
    fun splashViewModel_routesToMainOnTransientProfileFetchFailure() = runTest(dispatcher) {
        val repository = repositoryWithResponse(
            refresh = RefreshResponse("newA", "newR"),
            initialRefresh = "oldR",
            onboardingApi = FailingOnboardingApi()
        )
        val viewModel = SplashViewModel(repository)

        advanceUntilIdle()

        assertEquals(SplashDestination.MAIN, viewModel.uiState.value.destination)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    private fun repositoryWithResponse(
        login: AuthResponse = AuthResponse("a", "r"),
        register: AuthResponse = AuthResponse("a", "r"),
        refresh: RefreshResponse = RefreshResponse("a", "r"),
        initialRefresh: String? = null,
        onboardingApi: OnboardingApiService = SuccessfulOnboardingApi()
    ): AuthRepository {
        val tokenStorage = InMemoryTokenStorage(refreshToken = initialRefresh)
        return AuthRepository(
            authApiService = object : AuthApiService {
                override suspend fun register(request: RegisterRequest): AuthResponse = register
                override suspend fun login(request: LoginRequest): AuthResponse = login
                override suspend fun refresh(request: RefreshRequest): RefreshResponse = refresh
            },
            sessionStore = SessionStore(tokenStorage),
            errorMapper = AuthErrorMapper(),
            onboardingRepository = OnboardingRepository(onboardingApi),
            ioDispatcher = dispatcher
        )
    }

    private fun repositoryWithErrorOnRefresh(initialRefresh: String): AuthRepository {
        val tokenStorage = InMemoryTokenStorage(refreshToken = initialRefresh)
        return AuthRepository(
            authApiService = object : AuthApiService {
                override suspend fun register(request: RegisterRequest): AuthResponse = AuthResponse("a", "r")
                override suspend fun login(request: LoginRequest): AuthResponse = AuthResponse("a", "r")
                override suspend fun refresh(request: RefreshRequest): RefreshResponse {
                    throw java.io.IOException("network")
                }
            },
            sessionStore = SessionStore(tokenStorage),
            errorMapper = AuthErrorMapper(),
            onboardingRepository = OnboardingRepository(SuccessfulOnboardingApi()),
            ioDispatcher = dispatcher
        )
    }
}

private class BooleanInjuryOnboardingApi : OnboardingApiService {
    override suspend fun submitOnboarding(request: OnboardingRequest): OnboardingResponse = OnboardingResponse("u", "p")
    override suspend fun getProfile(): ProfileResponse = ProfileResponse(
        "u",
        "u@example.com",
        "Europe/Berlin",
        ProfileData(
            experienceLevel = "BEGINNER",
            typicalWeeklyDistanceKm = 20.0,
            longestRecentRunKm = 10.0,
            preferredRunDays = listOf("MONDAY", "WEDNESDAY", "SATURDAY"),
            preferredLongRunDay = "SATURDAY",
            goalStyle = "FINISH",
            strengthDaysPerWeek = 1,
            units = "KM",
            injuryHistory = mapOf(
                "hadRunningInjuryLast12Months" to JsonPrimitive(true),
                "summary" to JsonPrimitive("Recovered calf strain")
            )
        )
    )
}

private class SuccessfulOnboardingApi : OnboardingApiService {
    override suspend fun submitOnboarding(request: OnboardingRequest): OnboardingResponse = OnboardingResponse("u", "p")
    override suspend fun getProfile(): ProfileResponse = ProfileResponse(
        "u",
        "u@example.com",
        "Europe/Berlin",
        ProfileData(
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
}

private class MissingProfileOnboardingApi : OnboardingApiService {
    override suspend fun submitOnboarding(request: OnboardingRequest): OnboardingResponse = OnboardingResponse("u", "p")
    override suspend fun getProfile(): ProfileResponse {
        throw HttpException(Response.error<ProfileResponse>(404, "{}".toResponseBody("application/json".toMediaType())))
    }
}

private class FailingOnboardingApi : OnboardingApiService {
    override suspend fun submitOnboarding(request: OnboardingRequest): OnboardingResponse = OnboardingResponse("u", "p")
    override suspend fun getProfile(): ProfileResponse {
        throw IOException("network")
    }
}

private class InMemoryTokenStorage(
    accessToken: String? = null,
    refreshToken: String? = null
) : TokenStorage {
    private var access = accessToken
    private var refresh = refreshToken

    override fun readAccessToken(): String? = access
    override fun readRefreshToken(): String? = refresh
    override fun saveTokens(accessToken: String, refreshToken: String) {
        access = accessToken
        refresh = refreshToken
    }
    override fun clear() {
        access = null
        refresh = null
    }
}
