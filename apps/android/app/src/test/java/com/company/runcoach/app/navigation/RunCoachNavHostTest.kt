package com.company.runcoach.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RunCoachNavHostTest {

    @Test
    fun splashRouteIsStable() {
        assertEquals("splash", RunCoachRoutes.Splash)
    }
}
