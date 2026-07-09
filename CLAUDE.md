# CLAUDE.md

Guidance for Claude Code in this repository. Kept lean on purpose — this file is always loaded. Procedural/reference detail lives in `docs/` and per-layer conventions in `.claude/rules/` (loaded only when relevant).

## Project

Native Android app, package `com.seucaio.unideas`. UI 100% Jetpack Compose (no XML, no Fragments). **Multi-module** Gradle (Kotlin DSL): `:app` + `:domain`, `:data`, `:core:common`, `:core:ui`, `:core:backup`, `:feature:{home,items,sections,tags,settings}`.

- minSdk 24 · targetSdk/compileSdk 37 · Kotlin 2.2.10 · AGP 9.2.1 · Compose BOM 2026.02.01 · JVM 11
- Pre-MVP (`0.0.x` alpha). Dependency versions centralized in `gradle/libs.versions.toml` (`libs.*`) — add new deps there, not hardcoded.

## Commands

Run from repo root via the Gradle wrapper.

```
./gradlew build                 # full build
./gradlew assembleDebug         # build debug APK
./gradlew installDebug          # build + install debug APK on device/emulator
./gradlew test                  # local unit tests (JVM)
./gradlew test --tests "com.seucaio.unideas.ExampleUnitTest"   # single unit test class
./gradlew connectedAndroidTest  # instrumented tests (needs device/emulator)
./gradlew detekt                # static analysis (autocorrects; ignoreFailures — read the report)
./gradlew koverVerify           # coverage check (fails below min bound)
./gradlew lint                  # Android lint (reports only)
./gradlew clean                 # clean build outputs
```

## Architecture

Multi-module, MVI, no KMP. Full breakdown (package structure, dependency direction, Room schema) in **`docs/ARCHITECTURE.md`**; navigation in **`docs/FLOW.md`**.

- `:domain` — models + use cases; pure Kotlin, no Compose.
- `:data` — Room, DataStore, repository implementations.
- `:core:common` — shared utilities (no Compose). `:core:ui` — shared theme/components.
- `:core:backup` — Google Drive backup/restore, self-contained (scoped `GoogleSignIn` + Drive API, not Firebase Auth).
- `:feature:*` — one per screen area; depend on `:domain` + `:core:ui` only, **never `:data`** (implementations Koin-injected from `:app`).

## Code quality

- **Detekt** (`config/detekt/`): `autoCorrect` on, `ignoreFailures` on — read the report (`app/build/reports/detekt/`).
- **Kover**: 70% min via `koverVerify` on real logic (use cases, repos, mappers); excludes `*ViewModel*`, Composables and entry points. CI fails the PR if coverage drops below.
- **Lint**: `abortOnError = false` — reports only.

## Commits & branches

- **Commits**: [Conventional Commits](https://www.conventionalcommits.org/), **English**, `type: short description` (`feat`, `fix`, `build`, `chore`, `ci`, `docs`). Enforced by the `commit-msg` hook.
- **Branches**: feature branches cut from `dev`, target `dev`; `dev` periodically PRs into `main` (default branch). Never push directly to `main` (pre-push hook). PRs via the `open-pr` skill — title EN, body PT, diff vs the target branch (`git log dev..HEAD`).

## Conventions & rules

Coding conventions (MVI contract, ViewModel/use-case rules, testing, naming) live in **`docs/CONVENTIONS.md`**. The per-layer non-negotiables auto-load via **`.claude/rules/`**, scoped to `domain/**`, `data/**`, `feature/**` (+ `core/ui`, `core/backup`) — so they only enter context when you touch that layer.

## More docs

- **`docs/BLUEPRINT.md`** — class/screen inventory + implementation backlog (GitHub issues #3–#30, milestone `v0.1.0`).
- **`docs/RELEASE.md`** — build variants, signing, release automation, secrets, SemVer.
- **`docs/RUNNING.md`** — running/inspecting the app (`android` CLI) + git hooks (`./gradlew installGitHooks`).
