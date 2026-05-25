package com.company.runcoach.feature.profile.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.company.runcoach.feature.profile.ui.ProfileEditRoute

object ProfileRoutes {
    const val Edit = "profile_edit"
}

fun NavGraphBuilder.profileGraph(
    onOpenStrava: () -> Unit
) {
    composable(ProfileRoutes.Edit) {
        ProfileEditRoute(onOpenStrava = onOpenStrava)
    }
}
