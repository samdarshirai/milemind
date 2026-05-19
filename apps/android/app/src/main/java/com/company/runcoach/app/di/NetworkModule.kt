package com.company.runcoach.app.di

import com.company.runcoach.core.datastore.session.SecureTokenStorage
import com.company.runcoach.core.datastore.session.TokenStorage
import com.company.runcoach.core.network.ApiClient
import com.company.runcoach.feature.auth.data.AuthErrorMapper
import com.company.runcoach.feature.auth.data.remote.AuthApiService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
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
    fun provideAuthErrorMapper(): AuthErrorMapper = AuthErrorMapper()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {
    @Binds
    @Singleton
    abstract fun bindTokenStorage(impl: SecureTokenStorage): TokenStorage
}
