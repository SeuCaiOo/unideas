# AGENTS.md

This file provides guidance to AI coding agents when working with code in this repository.

## Project

Native Android app, package `com.seucaio.unideas`, single-module Gradle project (`:app`) built with the Kotlin DSL. UI is 100% Jetpack Compose (no XML layouts, no Fragments). Currently a freshly scaffolded default Android Studio Compose template — `MainActivity.kt` just shows a `Scaffold` + `Greeting` composable.

- Namespace / applicationId: `com.seucaio.unideas`
- minSdk 24, targetSdk/compileSdk 37
- Kotlin 2.2.10, AGP 9.2.1, Compose BOM 2026.02.01
- JVM target 11

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
./gradlew clean                 # clean build outputs
```

There is no dedicated ktlint/detekt config in this repo; rely on Android Studio/`./gradlew lint` for static checks.

## Architecture

- `app/src/main/java/com/seucaio/unideas/` — app code. `MainActivity.kt` is the single entry point (`ComponentActivity`) and hosts the Compose tree via `setContent`.
- `app/src/main/java/com/seucaio/unideas/ui/theme/` — Compose theming (`Theme.kt`, `Color.kt`, `Type.kt`) exposing `UnideasTheme { ... }`, applied at the root of `setContent`.
- `app/src/test/` — local JUnit unit tests (run on JVM).
- `app/src/androidTest/` — instrumented tests (run on device/emulator, use Espresso/Compose test APIs).
- Dependency versions are centralized in `gradle/libs.versions.toml` (version catalog) and referenced from `app/build.gradle.kts` via `libs.*`. Add new dependencies there rather than hardcoding coordinates in the module build file.
- `settings.gradle.kts` declares the single `:app` module; add new Gradle modules here if the project grows beyond one module.

As the app grows beyond the template, prefer keeping screens/composables organized under `ui/` (mirroring the existing `ui/theme/` convention) rather than dumping everything in the root package.