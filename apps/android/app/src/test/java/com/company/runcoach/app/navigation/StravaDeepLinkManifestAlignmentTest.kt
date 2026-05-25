package com.company.runcoach.app.navigation

import com.company.runcoach.feature.strava.ui.navigation.StravaRoutes
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class StravaDeepLinkManifestAlignmentTest {

    @Test
    fun manifestContainsAppLinkTargetUsedByStravaNavigation() {
        val manifest = readManifest()

        assertTrue(manifest.contains("android:scheme=\"https\""))
        assertTrue(manifest.contains("android:host=\"app.example.com\""))
        assertTrue(manifest.contains("android:path=\"/strava/connected\""))
        assertTrue(StravaRoutes.AppLinkCallbackPattern.startsWith("https://app.example.com/strava/connected"))
    }

    @Test
    fun manifestContainsCustomSchemeTargetUsedByStravaNavigation() {
        val manifest = readManifest()

        assertTrue(manifest.contains("android:scheme=\"milemind\""))
        assertTrue(manifest.contains("android:host=\"integrations\""))
        assertTrue(manifest.contains("android:path=\"/strava/callback\""))
        assertTrue(StravaRoutes.CustomSchemeCallbackPattern.startsWith("milemind://integrations/strava/callback"))
    }

    private fun readManifest(): String {
        val candidateFiles = listOf(
            File("src/main/AndroidManifest.xml"),
            File("apps/android/app/src/main/AndroidManifest.xml")
        )
        val manifestFile = candidateFiles.firstOrNull { it.exists() }
            ?: error("AndroidManifest.xml not found in expected locations.")
        return manifestFile.readText()
    }
}
