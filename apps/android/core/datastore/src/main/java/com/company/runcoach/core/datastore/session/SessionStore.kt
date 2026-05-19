package com.company.runcoach.core.datastore.session

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionStore @Inject constructor(
    private val tokenStorage: TokenStorage
) {
    fun accessTokenOrNull(): String? = tokenStorage.readAccessToken()

    fun refreshTokenOrNull(): String? = tokenStorage.readRefreshToken()

    fun hasRefreshToken(): Boolean = !refreshTokenOrNull().isNullOrBlank()

    fun save(accessToken: String, refreshToken: String) {
        tokenStorage.saveTokens(accessToken, refreshToken)
    }

    fun clear() {
        tokenStorage.clear()
    }
}
