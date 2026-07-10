# CLAUDE.md

Guidance for Claude Code in this repository. Kept lean on purpose — this file is always loaded. Procedural/reference detail lives in `docs/` and per-layer conventions in `.claude/rules/` (loaded only when relevant).

## Project

Native Android app, package `com.seucaio.unideas`. UI 100% Jetpack Compose (no XML, no Fragments). **Multi-module** Gradle (Kotlin DSL): `:app` + `:domain`, `:data`, `:core:common`, `:core:ui`, `:core:backup`, `:feature:{home,items,sections,tags,settings}`.

- minSdk 24 · targetSdk/compileSdk 37 · Kotlin 2.2.10 · AGP 9.2.1 · Compose BOM 2026.02.01 · JVM 11
- Pre-MVP (`0.0.x` alpha). Dependency versions centralized in `gradle/libs.versions.toml` (`libs.*`) — add new deps there, not hardcoded.

## Commands

Run from repo root via the Gradle wrapper.

**Always run `./gradlew clean` right before `detekt` or `koverVerify`.** Both tasks are cache-sensitive across this multi-module setup — a stale build/configuration cache can report a coverage percentage or lint state that doesn't match the actual code (seen firsthand: `koverVerify` failed at 31% from stale cache when the real, post-clean number was well above the 70% minimum). The project is small, so the clean costs a few extra seconds — cheap insurance against chasing a phantom failure. Don't skip it to save time.

```
./gradlew clean                 # run first, always, before detekt/koverVerify
./gradlew build                 # full build
./gradlew assembleDebug         # build debug APK
./gradlew installDebug          # build + install debug APK on device/emulator
./gradlew test                  # local unit tests (JVM)
./gradlew test --tests "com.seucaio.unideas.ExampleUnitTest"   # single unit test class
./gradlew connectedAndroidTest  # instrumented tests (needs device/emulator)
./gradlew detekt                # static analysis (autocorrects; ignoreFailures — read the report) — clean first
./gradlew koverVerify           # coverage check (fails below min bound) — clean first
./gradlew lint                  # Android lint (reports only)
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
- **Push confirmation**: on a feature branch, following the `open-pr`/`finish-issue` flow, Claude pushes without asking — the plan/PR checkpoints already validated the work. A direct commit on `main`/`dev` (the "commit pontual" exception, unreviewed by a PR) may still be pushed by Claude, but only after explicit user confirmation for that specific push — nothing in that commit went through a review gate, and undoing it once it's on a shared branch means a rebase. Enforced by a `PreToolUse`/`Bash` hook in `.claude/settings.json` (asks only when `git push` runs from `main`/`dev`).
- **Push ≠ merge authorization**: pushing a feature branch and enabling auto-merge on its PR are two different levels of consent. Push-without-asking (above) only covers getting the commit onto the remote as a backup — it does **not** imply the user has seen the code and is fine with it merging into `dev` unattended. Claude opens the PR and may push follow-up commits to it freely, but must ask before running `gh pr merge --auto`/`gh pr ready` (i.e. before arming the PR to merge on its own) — DoD being green is a self-check, not a substitute for the user actually looking at the diff. See `open-pr` step 7.

## Conventions & rules

Coding conventions (MVI contract, ViewModel/use-case rules, testing, naming) live in **`docs/CONVENTIONS.md`**. The per-layer non-negotiables auto-load via **`.claude/rules/`**, scoped to `domain/**`, `data/**`, `feature/**` (+ `core/ui`, `core/backup`) — so they only enter context when you touch that layer.

## More docs

- **`docs/BLUEPRINT.md`** — class/screen inventory + implementation backlog (GitHub issues #3–#30, milestone `v0.1.0`).
- **`docs/RELEASE.md`** — build variants, signing, release automation, secrets, SemVer.
- **`docs/RUNNING.md`** — running/inspecting the app (`android` CLI) + git hooks (`./gradlew installGitHooks`).
