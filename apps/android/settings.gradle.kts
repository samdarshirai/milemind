pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "runcoach-android"
include(":app")
include(":core:common")
include(":core:network")
include(":core:model")
include(":core:designsystem")
include(":core:database")
include(":core:datastore")
include(":feature:auth")
include(":feature:onboarding")
include(":feature:today")
include(":feature:plan")
include(":feature:workout")
include(":feature:checkin")
include(":feature:progress")
include(":feature:coach")
include(":feature:profile")
include(":feature:strava")
