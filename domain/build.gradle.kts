plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.seucaio.unideas.domain"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    testFixtures {
        enable = true
    }

    lint {
        // Lint doesn't apply coreLibraryDesugaring to the testFixtures component and
        // reports false-positive NewApi errors on java.time there. Main sources stay linted.
        ignoreTestFixturesSources = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.desugar.jdk.libs)
    implementation(libs.coroutines.android)
    implementation(libs.koin.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)

    testFixturesImplementation(libs.junit)

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
