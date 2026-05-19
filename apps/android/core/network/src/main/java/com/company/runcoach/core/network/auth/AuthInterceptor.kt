package com.company.runcoach.core.network.auth

import com.company.runcoach.core.datastore.session.SessionStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionStore: SessionStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = sessionStore.accessTokenOrNull()

        if (token.isNullOrBlank()) {
            return chain.proceed(original)
        }

        val authenticated = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticated)
    }
}
