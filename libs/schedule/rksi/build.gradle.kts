plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":libs:schedule:core"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.ktor)
    implementation(libs.ksoup.lite)
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
    testImplementation(libs.junit)
}
