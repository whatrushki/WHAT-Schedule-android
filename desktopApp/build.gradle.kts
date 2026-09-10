plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":libs:schedule:core"))
    implementation(project(":libs:schedule:rksi"))
    implementation(project(":libs:schedule:dgtu"))
    implementation(project(":libs:schedule:iubip"))
    implementation(project(":libs:schedule:rinh"))
    implementation(project(":core:ui"))

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.components.resources)

    implementation(libs.bundles.ktor)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
}

compose.desktop {
    application {
        mainClass = "app.what.schedule.desktop.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "WHAT-Schedule"
            packageVersion = "1.1.0"
            windows {
                menu = true
                shortcut = true
                menuGroup = "WHAT-Schedule"
            }
        }
    }
}
