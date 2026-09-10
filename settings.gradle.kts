pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://artifactory-external.vkpartner.ru/artifactory/maven")
        }
    }
}

rootProject.name = "WHAT-Schedule"
include(":app")
include(":core:foundation")
include(":core:navigation")
include(":core:ui")
include(":domain")
include(":data")
include(":features:main")
include(":features:schedule")
include(":features:settings")
include(":features:news")
include(":features:onboarding")
include(":features:dev")
include(":features:account")

include(":libs:schedule:core")
include(":libs:schedule:rksi")
include(":libs:schedule:dgtu")
include(":libs:schedule:iubip")
include(":libs:schedule:rinh")

include(":tools:schedule-sync")
include(":desktopApp")
include(":wasmApp")
include(":iosApp")
