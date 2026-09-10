plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("schedule-web")
        browser {
            commonWebpackConfig {
                outputFileName = "schedule-web.js"
            }
        }
        binaries.executable()
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
            implementation(libs.materialKolor)
        }
    }
}
