plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ScheduleKit"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(project(":libs:schedule:core"))
            implementation(project(":libs:schedule:rksi"))
            implementation(project(":libs:schedule:dgtu"))
            implementation(project(":libs:schedule:iubip"))
            implementation(project(":libs:schedule:rinh"))
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.bundles.ktor)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
