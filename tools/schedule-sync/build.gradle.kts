plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    application
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("app.what.tools.sync.MainKt")
}

dependencies {
    implementation(project(":libs:schedule:core"))
    implementation(project(":libs:schedule:rksi"))
    implementation(project(":libs:schedule:dgtu"))
    implementation(project(":libs:schedule:iubip"))
    implementation(project(":libs:schedule:rinh"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.ktor.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ksoup.lite)
}
