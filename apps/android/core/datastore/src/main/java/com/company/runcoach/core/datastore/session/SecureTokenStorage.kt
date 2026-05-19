package com.company.runcoach.core.datastore.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureTokenStorage @Inject constructor(
    @ApplicationContext context: Context
) : TokenStorage {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREF_FILE,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun readAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    override fun readRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    override fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    override fun clear() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).remove(KEY_REFRESH_TOKEN).apply()
    }

    private companion object {
        const val PREF_FILE = "runcoach_secure_session"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
