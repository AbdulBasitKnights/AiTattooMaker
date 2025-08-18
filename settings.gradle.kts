pluginManagement {
    plugins {
//        id("dagger.hilt.android.plugin") version "2.57" apply false
        id("androidx.navigation.safeargs.kotlin") version "2.9.3" apply false
    }
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.fabric.io/public")
        maven(url = "https://maven.google.com")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.fabric.io/public")
        maven(url = "https://maven.google.com")
    }
}

rootProject.name = "Ai Tattoo Maker"
include(":app")
include(":stickerlibrary")
