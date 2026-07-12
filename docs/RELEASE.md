# Release, signing & versioning

Reference for build variants, signing, the release automation and CI secrets. Not needed for day-to-day feature work — see `CLAUDE.md` for the always-on essentials.

## Build variants & signing

- `debug` builds get `applicationIdSuffix = ".debug"` and a distinct launcher name ("Unideas Debug", via `resValue`), so debug and release install side by side on the same device.
- `release` builds are signed via `signingConfigs.release` in `app/build.gradle.kts`, which reads `STORE_FILE_PATH`/`STORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD` env vars (CI) with a fallback to a local, gitignored `signing.properties`. The local keystore is `unideas-release.jks` (gitignored) — **back both up somewhere safe; losing them blocks future signed updates on the same app.**
- `release` builds run R8 (`optimization.enable = true`), which requires `android.r8.gradual.support=true` in `gradle.properties` (AGP 9's declarative build-type DSL).
- `buildFeatures.buildConfig = true` is enabled so `BuildConfig.VERSION_NAME`/`VERSION_CODE` are generated for the app module.

### Building each variant locally

```bash
./gradlew assembleDebug     # app/build/outputs/apk/debug/unideas-v<version>-debug.apk
./gradlew assembleRelease   # app/build/outputs/apk/release/unideas-v<version>.apk — needs signing.properties locally (or the CI env vars above)
```

### SHA-1 / SHA-256 fingerprints (Google Cloud Console / Firebase setup)

```bash
./gradlew signingReport
```

Reads every configured signing config (`debug` keystore, `signingConfigs.release`) directly from `app/build.gradle.kts` and prints `SHA1`/`SHA-256`/`MD5` per variant — no need to know keystore paths/passwords/aliases by hand. Needed whenever registering an Android OAuth client (Google Sign-In, Drive API, etc.) in Cloud Console/Firebase: one client per package name (`com.seucaio.unideas` / `com.seucaio.unideas.debug`) + matching SHA-1.

## Release automation

`prepare_release.yml` (workflow_dispatch, run manually from `main`) bumps `versionCode`/`versionName` in `app/build.gradle.kts`, commits, tags (`vX.Y.Z`), then calls `release_build.yml`, which builds a signed release APK, creates a draft GitHub Release and uploads to Firebase App Distribution (group `alpha-testers`). `release-drafter.yml` keeps a draft changelog up to date on every push to `main`, categorized by label via `.github/changelog-drafter.yml` — `release_build.yml` reuses that draft's body for the real release's notes (then deletes the draft) instead of GitHub's native auto-generated notes, to avoid a redundant "New Contributors" section on a single-dev repo.

- **APK naming**: `androidComponents.onVariants` in `app/build.gradle.kts` renames output APKs to `unideas-v<versionName>.apk` (`unideas-v<versionName>-debug.apk` for debug) instead of the generic `app-release.apk` — makes builds identifiable once several versions pile up on a device or in Downloads.

## Secrets (GitHub Actions)

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

## Versioning (SemVer)

| Version | Phase | Description |
|---|---|---|
| `0.0.x` | Alpha | Pre-MVP builds distributed via Firebase App Distribution (pre-releases) |
| `0.1.0` | MVP | Feature-complete for the first real milestone |
| `1.0.0` | Production | Validated, secure, stable (store) |

Release tracking: milestone **`v0.1.0 — MVP`** groups all MVP issues (progress bar); the `0.0.x` are the test builds cut along the way (tags + GitHub Releases), plus the board's `Released` column.
