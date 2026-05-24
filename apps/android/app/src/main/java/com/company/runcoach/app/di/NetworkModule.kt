package com.company.runcoach.app.di

import com.company.runcoach.core.datastore.session.SecureTokenStorage
import com.company.runcoach.core.datastore.session.TokenStorage
import com.company.runcoach.core.network.ApiClient
import com.company.runcoach.feature.auth.data.AuthErrorMapper
import com.company.runcoach.feature.auth.data.remote.AuthApiService
import com.company.runcoach.feature.onboarding.data.remote.OnboardingApiService
import com.company.runcoach.feature.plan.data.remote.PlanApiService
import com.company.runcoach.feature.profile.data.remote.ProfileApiService
import com.company.runcoach.feature.racegoal.data.remote.RaceGoalApiService
import com.company.runcoach.feature.workout.data.remote.WorkoutApiService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(apiClient: ApiClient): Retrofit = apiClient.retrofit

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideOnboardingApiService(retrofit: Retrofit): OnboardingApiService =
        retrofit.create(OnboardingApiService::class.java)

    @Provides
    @Singleton
    fun provideProfileApiService(retrofit: Retrofit): ProfileApiService =
        retrofit.create(ProfileApiService::class.java)

    @Provides
    @Singleton
    fun provideRaceGoalApiService(retrofit: Retrofit): RaceGoalApiService =
        retrofit.create(RaceGoalApiService::class.java)

    @Provides
    @Singleton
    fun providePlanApiService(retrofit: Retrofit): PlanApiService =
        retrofit.create(PlanApiService::class.java)

    @Provides
    @Singleton
    fun provideWorkoutApiService(retrofit: Retrofit): WorkoutApiService =
        retrofit.create(WorkoutApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthErrorMapper(json: Json): AuthErrorMapper = AuthErrorMapper(json)

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {
    @Binds
    @Singleton
    abstract fun bindTokenStorage(impl: SecureTokenStorage): TokenStorage
}
