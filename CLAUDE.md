# CLAUDE.md

Guidance for Claude Code in this repository. Kept lean on purpose — this file is always loaded. Procedural/reference detail lives in `docs/` and per-layer conventions in `.claude/rules/` (loaded only when relevant).

## Project

Native Android app, package `com.seucaio.unideas`. UI 100% Jetpack Compose (no XML, no Fragments). **Multi-module** Gradle (Kotlin DSL): `:app` + `:domain`, `:data`, `:core:common`, `:core:backup`, `:uds`, `:feature:{home,items,sections,tags,settings}`.

- minSdk 24 · targetSdk/compileSdk 37 · Kotlin 2.2.10 · AGP 9.2.1 · Compose BOM 2026.02.01 · JVM 11
- Pre-MVP (`0.0.x` alpha). Dependency versions centralized in `gradle/libs.versions.toml` (`libs.*`) — add new deps there, not hardcoded.

## Commands

Run from repo root via the Gradle wrapper.

**Always run `./gradlew clean` right before `detekt` or `koverVerify`.** Both tasks are cache-sensitive across this multi-module setup — a stale build/configuration cache can report a coverage percentage or lint state that doesn't match the actual code (seen firsthand: `koverVerify` failed at 31% from stale cache when the real, post-clean number was well above the 70% minimum). The project is small, so the clean costs a few extra seconds — cheap insurance against chasing a phantom failure. Don't skip it to save time.

**Never run `./gradlew build` while developing on a feature branch — use `assembleDebug` instead.** `build` runs the full task graph (debug + release, both variants' lint/tests, R8, dex, everything) and takes minutes; `assembleDebug` builds only what's needed to install and manually test on a device/emulator, in a fraction of the time, and only grows slower as the project does. `build` is reserved for `main` (the release branch) — that's the one place the full release graph actually needs to run.

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
- `:core:common` — shared utilities (no Compose).
- `:uds` — design system ported from another project (package `com.seucaio.unideas.ds`, #87), domain-agnostic (no `:domain`/`:core:common` dependency), Compose exposed via `api`. Replaced `:core:ui` entirely (#82 redesign epic) — all shared UI work goes here now. `uds/components/legacy/` holds components ported verbatim from the old `:core:ui` (some carry a documented exception to the module's "no `R.*` references" portability rule, since `legacy/` is transitional and will eventually be folded into the rest of `:uds` or removed) — see the module's README.
- `:core:backup` — Google Drive backup/restore, self-contained (scoped `GoogleSignIn` + Drive API, not Firebase Auth).
- `:feature:*` — one per screen area; depend on `:domain` + `:uds` only, **never `:data`** (implementations Koin-injected from `:app`).

## Code quality

- **Detekt** (`config/detekt/`): `autoCorrect` on, `ignoreFailures` on — read the report (`app/build/reports/detekt/`).
- **Kover**: 70% min via `koverVerify` on real logic — use cases, repos, mappers, **and ViewModels** (the `*ViewModel*` exclusion was removed as of #41; each tested `:feature:*` ViewModel must be added to `app/build.gradle.kts`'s `kover(project(...))` aggregation). Composables/PreviewProviders/entry points stay excluded. CI fails the PR if coverage drops below.
- **Lint**: `abortOnError = false` — reports only.

## Implementation workflow

Strict order for every implementation step on a plan item — no skipping, no reordering, no doing two at once:

1. **Plan** — write/update the plan (`.claude/plans/`).
2. **Code** — implement exactly what the plan says for that item. Then **stop**.
3. **Wait for the user to validate the code** — don't self-assess it as correct or say "vou validar." The user reviews it and tells you explicitly it's right. Nothing past this point happens until they do.
4. **Test** — only after that explicit validation, and only if the user actually asks for it (validation doesn't imply a test request). Never run the app/emulator against code the user hasn't validated — that tests something they never signed off on.
5. **Commit** — only after a requested test passes, or right after validation if no test was requested.
6. **Mark the plan item done** — only after a commit exists for it. Uncommitted code is not "done": it can be discarded at any point, so checking an item off without a backing commit misrepresents the project's real state.

Confirmed the hard way (2026-07-21): building/testing/marking-done a UI change before the user had looked at the code wasted real time on both sides when it turned out not to be what they wanted — they had to stop the flow and have it reverted. This applies project-wide, not just to one task.

**While an item is mid-implementation, GitHub stays untouched.** Progress updates during work go only into the local plan file (`.claude/plans/*.md`) — never into a GitHub issue's checklist, never a new GitHub issue, never the project board. GitHub sync is a separate step tied to the `open-pr`/`finish-issue` skills, at the point a PR actually exists — not something to do reactively mid-task just because a plan item feels finished. A checked box or a new issue on GitHub is a claim that work is real; if there's no push and no PR backing it, that claim is false. Confirmed the hard way (2026-07-21, issue #86): a checklist item got marked done on GitHub, and a new sub-issue got created via the `new-issue` skill, while the code was still local-only (commits ahead of `origin`, nothing pushed) — both had to be reverted; nobody had asked for either.

**Right before opening a PR, actually read the issue's DoD/checklist and check it against the diff.** Not a skim, not a rubber stamp — walk each checklist item and confirm it's genuinely backed by what's in the diff before checking it off in the `open-pr`/`finish-issue` DoD-reconciliation step.

## Commits & branches

- **Commits**: [Conventional Commits](https://www.conventionalcommits.org/), **English**, `type: short description` (`feat`, `fix`, `build`, `chore`, `ci`, `docs`). Enforced by the `commit-msg` hook.
- **Branches**: feature branches cut from `dev`, target `dev`; `dev` periodically PRs into `main` (default branch). Never push directly to `main` (pre-push hook). PRs via the `open-pr` skill — title EN, body PT, diff vs the target branch (`git log dev..HEAD`).
- **Long-lived epic branches** (exception to the rule above): a large multi-issue epic — e.g. the #82 redesign — runs on its own long-lived branch cut from `dev` (`feature/82-redesign-ui-ux`), not directly on `dev`. Every sub-issue branch targets the epic branch as base instead of `dev`; if `dev` moves during the epic, sync with a "mergeback" PR (`dev` → epic branch); only when the whole epic is done does the epic branch PR into `dev`. Same pattern used in GymLog (`feature/v2-modules`).
- **Commit confirmation**: `git commit` is **never automatic** on feature-branch development work — always ask before committing code with dev scope (new/changed use cases, ViewModels, tests tied to a feature, etc.), same as any other commit. The only exception is a purely mechanical change with no associated dev scope (a standalone docs edit, a skill-file fix, a one-line config tweak) — those may be committed without asking. When in doubt about whether something counts as "dev scope," ask.
- **Push confirmation**: this is a separate question from the commit above, and only applies once a commit already exists. On a feature branch, following the `open-pr`/`finish-issue` flow, Claude pushes an *already-confirmed* commit without asking again — the plan/PR checkpoints already validated the work, so re-asking would be redundant. A direct commit on `main`/`dev` (the "commit pontual" exception, unreviewed by a PR) may still be pushed by Claude, but only after explicit user confirmation for that specific push — nothing in that commit went through a review gate, and undoing it once it's on a shared branch means a rebase. **Exception: docs-only commits** (touching only `docs/`, `CLAUDE.md`, `AGENTS.md`, `.claude/rules/`) push straight through without asking — no dev scope, nothing to review. Anything else, including a change to `.claude/settings.json` itself, still asks. Enforced by a `PreToolUse`/`Bash` hook in `.claude/settings.json` (checks the pushed diff against `origin/<branch>` when `git push` runs from `main`/`dev`).
- **Every PR opens as Draft, always** — not a default that DoD status can override. Push-without-asking (above) only covers getting commits onto the remote as a backup; it does **not** extend to making a PR mergeable. Promoting a Draft to ready and arming auto-merge (`gh pr ready` / `gh pr merge --auto`) is the user's decision alone, asked explicitly every time, with no exception for "DoD passed" — DoD green is Claude's self-check that the work matches the checklist, not the user having looked at the diff. Confirmed the hard way: PR #38 (issue #24) got auto-merge armed the instant it was created, no review window at all. See `open-pr` steps 6–7 and `finish-issue`.

## Conventions & rules

Coding conventions (MVI contract, ViewModel/use-case rules, testing, naming) live in **`docs/CONVENTIONS.md`**. The per-layer non-negotiables auto-load via **`.claude/rules/`**, scoped to `domain/**`, `data/**`, `feature/**` (+ `core/backup`) — so they only enter context when you touch that layer.

## More docs

- **`docs/BLUEPRINT.md`** — original class/screen inventory + implementation backlog, frozen as planning context. Live issue/PR status lives in the "unideas — Improvements" artifact + the GitHub Project board, not here.
- **`docs/RELEASE.md`** — build variants, signing, release automation, secrets, SemVer.
- **`docs/RUNNING.md`** — running/inspecting the app (`android` CLI) + git hooks (`./gradlew installGitHooks`).
