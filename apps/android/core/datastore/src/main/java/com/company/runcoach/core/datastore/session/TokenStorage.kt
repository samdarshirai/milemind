package com.company.runcoach.core.datastore.session

interface TokenStorage {
    fun readAccessToken(): String?
    fun readRefreshToken(): String?
    fun saveTokens(accessToken: String, refreshToken: String)
    fun clear()
}
