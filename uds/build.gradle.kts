plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.seucaio.unideas.ds"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // api, not implementation: this module is the project's UI foundation (replacing
    // :core:ui) — every :feature:* module that depends on :uds inherits Compose
    // transitively, instead of redeclaring the BOM/artifacts in every feature's own
    // build.gradle.kts.
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    detektPlugins(libs.bundles.detekt)
}

detekt {
    config.setFrom(
        files(
            "$rootDir/config/detekt/detekt.yml",
            "$rootDir/config/detekt/detekt-compose.yml"
        )
    )
    toolVersion = libs.versions.detekt.get()
    buildUponDefaultConfig = true
    ignoreFailures = true
    parallel = true
    autoCorrect = true
}
