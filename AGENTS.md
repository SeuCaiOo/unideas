# AGENTS.md

This file provides guidance to AI coding agents when working with code in this repository.

## Project

Native Android app, package `com.seucaio.unideas`, single-module Gradle project (`:app`) built with the Kotlin DSL. UI is 100% Jetpack Compose (no XML layouts, no Fragments). Still close to the default Android Studio Compose template — `MainActivity.kt` shows a `Scaffold` with a `Greeting` composable and an `AppVersionFooter` in the bottom bar.

- Namespace / applicationId: `com.seucaio.unideas`
- minSdk 24, targetSdk/compileSdk 37
- Kotlin 2.2.10, AGP 9.2.1, Compose BOM 2026.02.01
- JVM target 11
- versionName currently `0.0.1` (pre-MVP)

## Commands

Run all commands from the repo root using the Gradle wrapper.

```
./gradlew build                 # full build
./gradlew assembleDebug         # build debug APK
./gradlew installDebug          # build and install debug APK on connected device/emulator
./gradlew test                  # run local unit tests (JVM, app/src/test)
./gradlew testDebugUnitTest     # unit tests for debug variant only
./gradlew test --tests "com.seucaio.unideas.ExampleUnitTest"   # run a single unit test class
./gradlew connectedAndroidTest  # run instrumented tests (app/src/androidTest), needs a device/emulator
./gradlew lint                  # Android lint
./gradlew detekt                # static analysis (autocorrects formatting; see Code quality below)
./gradlew koverVerify            # test coverage check (fails if below the min bound)
./gradlew koverHtmlReport        # HTML coverage report (app/build/reports/kover/)
./gradlew clean                 # clean build outputs
```

## Architecture

- `app/src/main/java/com/seucaio/unideas/` — app code. `MainActivity.kt` is the single entry point (`ComponentActivity`) and hosts the Compose tree via `setContent`.
- `app/src/main/java/com/seucaio/unideas/ui/theme/` — Compose theming (`Theme.kt`, `Color.kt`, `Type.kt`) exposing `UnideasTheme { ... }`, applied at the root of `setContent`.
- `app/src/main/java/com/seucaio/unideas/ui/components/` — small reusable composables not tied to a single screen, e.g. `AppVersionFooter` (shows `BuildConfig.VERSION_NAME`, mounted as the `Scaffold` bottom bar to visually confirm which build is running).
- `app/src/test/` — local JUnit unit tests (run on JVM).
- `app/src/androidTest/` — instrumented tests (run on device/emulator, use Espresso/Compose test APIs).
- Dependency versions are centralized in `gradle/libs.versions.toml` (version catalog) and referenced from `app/build.gradle.kts` via `libs.*`. Add new dependencies there rather than hardcoding coordinates in the module build file. Related libraries are grouped into `[bundles]` (`composeUi`, `composeDebug`, `androidTest`).
- `settings.gradle.kts` declares the single `:app` module; add new Gradle modules here if the project grows beyond one module.

As the app grows beyond the template, prefer keeping screens/composables organized under `ui/` (mirroring the existing `ui/theme/` and `ui/components/` convention) rather than dumping everything in the root package.

## Build variants & signing

- `debug` builds get `applicationIdSuffix = ".debug"` and a distinct launcher name ("Unideas Debug", via `resValue`), so debug and release can be installed side by side on the same device.
- `release` builds are signed via `signingConfigs.release` in `app/build.gradle.kts`, which reads `STORE_FILE_PATH`/`STORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD` env vars (CI) with a fallback to a local, gitignored `signing.properties`. The local keystore is `unideas-release.jks` (gitignored) — back both up somewhere safe; losing them blocks future signed updates on the same app.
- `release` builds run R8 (`optimization.enable = true`), which requires `android.r8.gradual.support=true` in `gradle.properties` (AGP 9's new declarative build-type DSL).
- `buildFeatures.buildConfig = true` is enabled so `BuildConfig.VERSION_NAME`/`VERSION_CODE` are generated for the app module.

## Code quality

- **Detekt**: configured in `app/build.gradle.kts`, config from `config/detekt/detekt.yml` (generated baseline) + `config/detekt/detekt-compose.yml` (Compose-specific rules, via the `detekt-compose-rules` plugin). `autoCorrect = true` fixes formatting issues (imports, final newline, etc.) in place; `ignoreFailures = true` means `./gradlew detekt` never fails the build — read the console output/HTML report (`app/build/reports/detekt/`) to catch real issues.
- **Kover**: configured in `app/build.gradle.kts`. Excludes `BuildConfig`/`Application`/`Activity`/`*ViewModel*`/`ui.theme` and anything annotated `@Composable`/`@Preview`/`@PreviewLightDark` from coverage — only real logic (use cases, repositories, mappers, ViewModels' non-Composable logic once introduced) counts. `koverVerify` enforces a 70% minimum; adjust `minBound` in `app/build.gradle.kts` as the codebase matures.
- **Lint**: `abortOnError = false` in `app/build.gradle.kts` — `./gradlew lint` reports but doesn't fail the build; check `app/build/reports/lint-results-debug.html`.