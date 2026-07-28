plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.seucaio.unideas.core.notifications"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:common"))

    implementation(libs.work.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.koin.android)
    implementation(libs.koin.workmanager)
    implementation(libs.coroutines.android)
    implementation(libs.timber)

    coreLibraryDesugaring(libs.android.desugar.jdk.libs)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(testFixtures(project(":domain")))

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.work.testing)
    androidTestImplementation(libs.coroutines.test)

    detektPlugins(libs.bundles.detekt)
}

detekt {
    config.setFrom(
        files("$rootDir/config/detekt/detekt.yml")
    )
    toolVersion = libs.versions.detekt.get()
    buildUponDefaultConfig = true
    ignoreFailures = true
    parallel = true
    autoCorrect = true
}
