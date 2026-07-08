# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

Multi-module, MVI, no KMP — see `docs/ARCHITECTURE.md` for the full breakdown (module dependency direction, conventions, Room schema draft, Google Drive backup approach).

- `:app` — entry point, Koin DI wiring, `MainActivity.kt` (`ComponentActivity`) hosts the Compose tree via `setContent`. Still has the template's `ui/theme/` (`Theme.kt`/`Color.kt`/`Type.kt`, exposing `UnideasTheme { ... }`) and `ui/components/` (`AppVersionFooter`) — these are slated to move into `:core:ui` as that module gets real content, not yet done.
- `:domain` — models + use cases, pure Kotlin/Android, no Compose.
- `:data` — Room, DataStore, repository implementations.
- `:core:common` — shared utilities, no Compose.
- `:core:ui` — shared theme/components (currently empty — see above).
- `:core:backup` — Google Drive backup/restore, self-contained (mirrors the GymLog reference project's `GoogleSignIn` + Drive API pattern, not Firebase Auth).
- `:feature:home` / `:feature:items` / `:feature:sections` / `:feature:tags` / `:feature:settings` — one per screen area. Depend on `:domain` + `:core:ui` only, never on `:data` directly (implementations are Koin-injected from `:app`).
- `app/src/test/` — local JUnit unit tests (run on JVM). `app/src/androidTest/` — instrumented tests.
- Dependency versions are centralized in `gradle/libs.versions.toml` (version catalog) and referenced via `libs.*` from every module's `build.gradle.kts`. Add new dependencies there rather than hardcoding coordinates.

## Build variants & signing

- `debug` builds get `applicationIdSuffix = ".debug"` and a distinct launcher name ("Unideas Debug", via `resValue`), so debug and release can be installed side by side on the same device.
- `release` builds are signed via `signingConfigs.release` in `app/build.gradle.kts`, which reads `STORE_FILE_PATH`/`STORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD` env vars (CI) with a fallback to a local, gitignored `signing.properties`. The local keystore is `unideas-release.jks` (gitignored) — back both up somewhere safe; losing them blocks future signed updates on the same app.
- `release` builds run R8 (`optimization.enable = true`), which requires `android.r8.gradual.support=true` in `gradle.properties` (AGP 9's new declarative build-type DSL).
- `buildFeatures.buildConfig = true` is enabled so `BuildConfig.VERSION_NAME`/`VERSION_CODE` are generated for the app module.

## Release automation

`prepare_release.yml` (workflow_dispatch, run manually from `main`) bumps `versionCode`/`versionName` in `app/build.gradle.kts`, commits, tags (`vX.Y.Z`), then calls `release_build.yml`, which builds a signed release APK, creates a draft GitHub Release and uploads to Firebase App Distribution (group `alpha-testers`). `release-drafter.yml` keeps a draft changelog up to date on every push to `main`, categorized by label via `.github/changelog-drafter.yml` — `release_build.yml` reuses that draft's body for the real release's notes (then deletes the draft) instead of GitHub's native auto-generated notes, to avoid a redundant "New Contributors" section on a single-dev repo.
- **APK naming**: `androidComponents.onVariants` in `app/build.gradle.kts` renames output APKs to `unideas-v<versionName>.apk` (`unideas-v<versionName>-debug.apk` for debug) instead of the generic `app-release.apk` — makes builds identifiable once several versions/apps pile up on a device or in Downloads.

### Secrets (GitHub Actions)

Required in `Settings → Secrets → Actions`:

| Secret | How to generate |
|---|---|
| `GOOGLE_SERVICES_JSON` | `base64 -w 0 app/google-services.json` |
| `KEYSTORE_BASE64` | `base64 -w 0 unideas-release.jks` |
| `STORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |
| `FIREBASE_APP_ID` | `firebase apps:list` (Android app, release variant — `com.seucaio.unideas`, not `.debug`) |
| `FIREBASE_TOKEN` | `firebase login:ci` |
| `GH_PAT` | GitHub → Settings → Developer settings → Personal access tokens (scope `repo`) — needed because commits/tags pushed with the default `GITHUB_TOKEN` don't trigger other workflows (e.g. `release-drafter`) |

### Versioning (SemVer)

| Version | Phase | Description |
|---|---|---|
| `0.0.x` | Alpha | Pre-MVP builds distributed via Firebase App Distribution |
| `0.1.0` | MVP | Feature-complete for the first real milestone |
| `1.0.0` | Production | Validated, secure, stable |

## Code quality

- **Detekt**: configured in `app/build.gradle.kts`, config from `config/detekt/detekt.yml` (generated baseline) + `config/detekt/detekt-compose.yml` (Compose-specific rules, via the `detekt-compose-rules` plugin). `autoCorrect = true` fixes formatting issues (imports, final newline, etc.) in place; `ignoreFailures = true` means `./gradlew detekt` never fails the build — read the console output/HTML report (`app/build/reports/detekt/`) to catch real issues.
- **Kover**: configured in `app/build.gradle.kts`. Excludes `BuildConfig`/`Application`/`Activity`/`*ViewModel*`/`ui.theme` and anything annotated `@Composable`/`@Preview`/`@PreviewLightDark` from coverage — only real logic (use cases, repositories, mappers, ViewModels' non-Composable logic once introduced) counts. `koverVerify` enforces a 70% minimum; adjust `minBound` in `app/build.gradle.kts` as the codebase matures.
- **Lint**: `abortOnError = false` in `app/build.gradle.kts` — `./gradlew lint` reports but doesn't fail the build; check `app/build/reports/lint-results-debug.html`.

## Commits & branches

- **Commit messages**: [Conventional Commits](https://www.conventionalcommits.org/), in English — `type: short description` (e.g. `feat: add login screen`, `build: add Firebase Crashlytics and Analytics`). Types used so far: `feat`, `fix`, `build`, `chore`, `ci`, `docs`. Enforced by the `commit-msg` git hook (see below).
- **Branches**: feature branches are cut from `dev` and target `dev` in their PR; `dev` periodically opens a PR into `main`. `main` stays the GitHub default branch. Never push directly to `main` — enforced by the `pre-push` git hook.
- **PRs**: use the `open-pr` skill (`.claude/skills/open-pr/`) — PR titles in English, description body in Portuguese, diff compared against the target branch only (`git log dev..HEAD`, never `main`). Fill in `.github/PULL_REQUEST_TEMPLATE.md`.

## Git Hooks

Hooks live in `.githooks/` and are activated per clone with:

```bash
./gradlew installGitHooks
```

| Hook | When | What it does |
|---|---|---|
| `pre-commit` | Before commit | Detekt (autoCorrect) on staged `.kt` files + blocks `google-services.json` |
| `commit-msg` | On commit | Validates Conventional Commits format |
| `pre-push` | Before push | Kotlin compilation check + Detekt + blocks push to `main` |

## Running & inspecting the app (`android` CLI)

Prefer the `android` CLI over raw `adb` for deploying and inspecting the app — it wraps `adb`/`aapt`/emulator control with commands purpose-built for this workflow.

```
android emulator list                              # list AVDs (add --long for status/API level)
android emulator start <avd-name>                  # boot an emulator, blocks until ready
android emulator stop <avd-name>                    # graceful shutdown (omit name if only one is running)
android run --apks=<path-to-apk> --activity=<pkg>.<Activity>   # install + launch (activity name WITHOUT the debug applicationIdSuffix)
android screen capture -o <path.png>                # screenshot; -a annotates UI elements with bounding boxes
android layout -p                                    # dump the current on-screen view tree as JSON (text + coordinates)
android describe                                     # locate build artifacts (APK paths, etc.) for a project
```

Notes learned by using it on this project:
- `--activity` takes the manifest-declared component name (e.g. `com.seucaio.unideas.MainActivity`), not the suffixed `applicationId` — the debug variant still installs as `com.seucaio.unideas.debug`, but the activity class name is unaffected.
- The local AVD (`Resizable_Experimental_API_34`) needs a few GB of free disk to boot; if `android emulator start` fails with "not enough disk space", check `df -h /home` before assuming it's a CLI bug — the culprit here was stale `~/.gradle/caches/<old-version>` folders (safe to delete; Gradle re-downloads on demand).