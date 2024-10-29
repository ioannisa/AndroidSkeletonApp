pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://jitpack.io")
    }

    // Applying the version to the Hilt plugin
    resolutionStrategy.eachPlugin {
        if (requested.id.namespace == "com.google.dagger") { useVersion("2.48.1") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "Demo Compose"
include(":app")
include(":coredata")
