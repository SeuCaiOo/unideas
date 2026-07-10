import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

// Release signing: prefers CI env vars (STORE_FILE_PATH/STORE_PASSWORD/KEY_ALIAS/KEY_PASSWORD),
// falls back to a local, gitignored signing.properties. Neither is required for debug builds.
val keystorePropertiesFile = rootProject.file("signing.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.seucaio.unideas"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.seucaio.unideas"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "0.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val envStoreFilePath = System.getenv("STORE_FILE_PATH")
            val envStorePassword = System.getenv("STORE_PASSWORD")
            val envKeyAlias = System.getenv("KEY_ALIAS")
            val envKeyPassword = System.getenv("KEY_PASSWORD")

            storeFile = (envStoreFilePath ?: keystoreProperties.getProperty("storeFilePath"))?.let {
                rootProject.file(it)
            }
            storePassword = envStorePassword ?: keystoreProperties.getProperty("storePassword")
            keyAlias = envKeyAlias ?: keystoreProperties.getProperty("keyAlias")
            keyPassword = envKeyPassword ?: keystoreProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        // debug and release coexist on the same device: distinct applicationId + launcher name.
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "Unideas Debug")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
        resValues = true
        buildConfig = true
    }
    lint {
        abortOnError = false
    }
}

// Distinguishable APK filenames (unideas-v<version>[-debug].apk) instead of the
// generic app-debug.apk/app-release.apk — several apps/variants can end up
// side by side on a device or in a Downloads folder.
androidComponents {
    onVariants { variant ->
        val suffix = if (variant.buildType == "debug") "-debug" else ""
        variant.outputs.forEach { output ->
            output.outputFileName.set("unideas-v${android.defaultConfig.versionName}$suffix.apk")
        }
    }
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

kover {
    reports {
        total {
            log { onCheck = true }
            html { onCheck = true }
            xml { onCheck = true }
        }
        filters {
            excludes {
                classes(
                    "*BuildConfig*",
                    // App entry points
                    "*Application*",
                    "*Activity*",
                    "*PreviewProvider",
                    // Compose generated classes
                    "*ComposableSingletons*",
                    // Koin modules
                    "*Module*",
                )

                packages(
                    "*.ui.theme",
                    "*.local.dao",
                    "*.local.database",
                    "*.local.converter",
                )

                annotatedBy(
                    "androidx.compose.runtime.Composable",
                    "androidx.compose.ui.tooling.preview.Preview",
                    "androidx.compose.ui.tooling.preview.PreviewLightDark"
                )
            }
        }
        verify {
            rule("Rule of coverage minimum for the project") {
                minBound(70)
            }
        }
    }
}

dependencies {
    // Aggregates coverage from real-logic modules into :app's koverVerify —
    // :app itself holds only entry points/Composables, all excluded above.
    kover(project(":domain"))
    kover(project(":data"))
    kover(project(":core:common"))
    kover(project(":feature:sections"))
    kover(project(":feature:tags"))
    kover(project(":feature:settings"))

    implementation(project(":core:ui"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":feature:sections"))
    implementation(project(":feature:tags"))
    coreLibraryDesugaring(libs.android.desugar.jdk.libs)

    // Compose BOM — aligns versions for every Compose artifact below
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // Compose UI
    implementation(libs.bundles.composeUi)

    // DI (Koin)
    implementation(libs.koin.android)

    // Logging
    implementation(libs.timber)

    // Firebase
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.google.firebase.crashlytics)
    implementation(libs.google.firebase.analytics)

    // Unit tests (JVM)
    testImplementation(libs.junit)

    // Instrumented tests (device/emulator)
    androidTestImplementation(libs.bundles.androidTest)

    // Debug-only tooling
    debugImplementation(libs.bundles.composeDebug)

    detektPlugins(libs.bundles.detekt)
}