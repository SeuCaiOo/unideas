plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.seucaio.unideas.core.backup"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    // Exception to the ":feature:* never depends on :data" rule — backup manipulates the
    // Room database file directly (close/checkpoint/path), not just via a repository interface.
    implementation(project(":data"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.room.runtime)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.coroutines.android)
    implementation(libs.timber)

    // Google Drive backup (scoped GoogleSignIn + Drive API, not Firebase Auth)
    implementation(libs.google.auth.identity)
    implementation(libs.google.drive.api)
    implementation(libs.google.http.client.android)
    implementation(libs.google.api.client.android)

    coreLibraryDesugaring(libs.android.desugar.jdk.libs)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)

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
