import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
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
        versionCode = 1
        versionName = "0.0.1"

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
    }
    buildFeatures {
        compose = true
        resValues = true
        buildConfig = true
    }
}

dependencies {
    // Compose BOM — aligns versions for every Compose artifact below
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose UI
    implementation(libs.bundles.composeUi)

    // Unit tests (JVM)
    testImplementation(libs.junit)

    // Instrumented tests (device/emulator)
    androidTestImplementation(libs.bundles.androidTest)

    // Debug-only tooling
    debugImplementation(libs.bundles.composeDebug)
}