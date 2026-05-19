package com.company.runcoach.app.navigation

import com.company.runcoach.feature.auth.ui.navigation.AuthRoutes
import org.junit.Assert.assertEquals
import org.junit.Test

class RunCoachNavHostTest {

    @Test
    fun splashRouteIsStable() {
        assertEquals("splash", AuthRoutes.Splash)
    }
}
