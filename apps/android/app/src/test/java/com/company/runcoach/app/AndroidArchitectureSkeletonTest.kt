package com.company.runcoach.app

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AndroidArchitectureSkeletonTest {

    @Test
    fun featurePackageMarkersExist() {
        val featureRoots = listOf(
            "auth", "onboarding", "today", "plan", "workout",
            "checkin", "progress", "coach", "profile", "strava"
        )

        val subpackages = listOf(
            "ui.PackageMarker",
            "ui.components.PackageMarker",
            "ui.model.PackageMarker",
            "ui.navigation.PackageMarker",
            "domain.PackageMarker",
            "data.PackageMarker",
            "data.remote.PackageMarker",
            "data.local.PackageMarker",
            "data.mapper.PackageMarker"
        )

        val androidRoot = File("..").canonicalFile
        assertNotNull(androidRoot)
        for (feature in featureRoots) {
            for (subpackage in subpackages) {
                val relativeDir = "feature/$feature/src/main/java/com/company/runcoach/feature/$feature/${subpackage.removeSuffix(".PackageMarker").replace('.', '/')}"
                val marker = File(androidRoot, "$relativeDir/PackageMarker.kt")
                assertTrue("Missing marker file: ${marker.path}", marker.exists())
            }
        }
    }
}
