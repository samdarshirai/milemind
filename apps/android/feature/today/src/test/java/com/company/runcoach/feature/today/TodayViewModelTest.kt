package com.company.runcoach.feature.today

import org.junit.Assert.assertNotNull
import org.junit.Test

class TodayViewModelTest {

    @Test
    fun viewModel_instantiatesForSkeleton() {
        assertNotNull(TodayViewModel())
    }
}
