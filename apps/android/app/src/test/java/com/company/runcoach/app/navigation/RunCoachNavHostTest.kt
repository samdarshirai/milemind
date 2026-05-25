package com.company.runcoach.app.navigation

import com.company.runcoach.feature.auth.ui.navigation.AuthRoutes
import com.company.runcoach.feature.progress.ui.navigation.ProgressRoutes
import com.company.runcoach.feature.today.ui.navigation.TodayRoutes
import org.junit.Assert.assertEquals
import org.junit.Test

class RunCoachNavHostTest {

    @Test
    fun splashRouteIsStable() {
        assertEquals("splash", AuthRoutes.Splash)
    }

    @Test
    fun todayHomeRoute_isStable() {
        assertEquals("today_home", TodayRoutes.HomeBase)
    }

    @Test
    fun progressRoute_isStable() {
        assertEquals("progress_overview", ProgressRoutes.Overview)
    }
}
