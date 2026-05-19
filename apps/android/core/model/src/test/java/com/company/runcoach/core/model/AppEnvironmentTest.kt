package com.company.runcoach.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppEnvironmentTest {

    @Test
    fun localEnvironmentIsDefined() {
        assertEquals(AppEnvironment.LOCAL, AppEnvironment.valueOf("LOCAL"))
        assertTrue(AppEnvironment.entries.contains(AppEnvironment.LOCAL))
    }
}
