# CLAUDE.md

Guidance for Claude Code in this repository. Kept lean on purpose — this file is always loaded. Procedural/reference detail lives in `docs/` and per-layer conventions in `.claude/rules/` (loaded only when relevant).

## Project

Native Android app, package `com.seucaio.unideas`. UI 100% Jetpack Compose (no XML, no Fragments). **Multi-module** Gradle (Kotlin DSL): `:app` + `:domain`, `:data`, `:core:common`, `:core:backup`, `:core:notifications`, `:uds`, `:feature:{home,items,sections,tags,settings}`.

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
- `:core:notifications` (#95) — reminder notifications: `PeriodicWorkRequest` 4x/day (`ReminderCheckWorker`/`ReminderScheduler`), per-item notifications grouped by urgency tier (`ReminderNotifier`, 2 channels: dismissible normal / non-dismissible urgent), deep link to `ItemsRoute.Detail` on tap (`unideas://item/{id}`, handled in `:app`'s `MainActivity`). Since #96, each scan also runs `ProcessMissedOccurrencesUseCase` — advances `dueDate` for overdue recurring items and logs missed occurrences, not just notification-only anymore.
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
6. **Mark the plan item done in the plan file** — check the box in `.claude/plans/*.md` itself, only after a commit exists for it. Uncommitted code is not "done": it can be discarded at any point, so checking an item off without a backing commit misrepresents the project's real state. Do this every time, not just when it happens to come up — the plan file's checklist is the source of truth `finish-issue`/`open-pr` read from later to reconcile against the issue's DoD, instead of re-deriving what's done from the diff each time.

Confirmed the hard way (2026-07-21): building/testing/marking-done a UI change before the user had looked at the code wasted real time on both sides when it turned out not to be what they wanted — they had to stop the flow and have it reverted. This applies project-wide, not just to one task.

**While an item is mid-implementation, GitHub stays untouched.** Progress updates during work go only into the local plan file (`.claude/plans/*.md`) — never into a GitHub issue's checklist, never a new GitHub issue, never the project board. GitHub sync is a separate step tied to the `open-pr`/`finish-issue` skills, at the point a PR actually exists — not something to do reactively mid-task just because a plan item feels finished. A checked box or a new issue on GitHub is a claim that work is real; if there's no push and no PR backing it, that claim is false. Confirmed the hard way (2026-07-21, issue #86): a checklist item got marked done on GitHub, and a new sub-issue got created via the `new-issue` skill, while the code was still local-only (commits ahead of `origin`, nothing pushed) — both had to be reverted; nobody had asked for either.

**Right before opening a PR, actually read the issue's DoD/checklist and check it against the diff.** Not a skim, not a rubber stamp — walk each checklist item and confirm it's genuinely backed by what's in the diff before checking it off in the `open-pr`/`finish-issue` DoD-reconciliation step.

**On "continua"/"onde paramos"** (or any bare resume request): opening a plan file is the trigger for the resume protocol in `.claude/rules/resume-work.md` (branch → checklist → PR → epic, asking before acting at each step) — don't jump into coding on a plan whose checklist hasn't been confirmed as the next step.

## Commits & branches

- **Commits**: [Conventional Commits](https://www.conventionalcommits.org/), **English**, `type: short description` (`feat`, `fix`, `build`, `chore`, `ci`, `docs`). Enforced by the `commit-msg` hook.
- **Branches**: feature branches cut from `dev`, target `dev`; `dev` periodically PRs into `main` (default branch). Never push directly to `main` (pre-push hook). PRs via the `open-pr` skill — title EN, body PT, diff vs the target branch (`git log dev..HEAD`).
- **Long-lived epic branches** (exception to the rule above): a large multi-issue epic — e.g. the #82 redesign — runs on its own long-lived branch cut from `dev` (`feature/82-redesign-ui-ux`), not directly on `dev`. Every sub-issue branch targets the epic branch as base instead of `dev`; if `dev` moves during the epic, sync with a "mergeback" PR (`dev` → epic branch); only when the whole epic is done does the epic branch PR into `dev`. Same pattern used in GymLog (`feature/v2-modules`).
  - **The branch must be created the moment an issue is declared an epic** — not deferred to whenever its first sub-issue happens to start. Confirmed the hard way (#96): the issue's own body said "vira epic, mesmo padrão de #82/#95," but no `feature/96-*` branch was ever created, so `start-feature` silently fell back to `dev` for its first sub-issue (#126) — nobody caught it until the PR was already open. Declaring an issue an epic and creating its branch are the same step, not two.
- **Commit confirmation**: `git commit` is **never automatic** on feature-branch development work — always ask before committing code with dev scope (new/changed use cases, ViewModels, tests tied to a feature, etc.), same as any other commit. The only exception is a purely mechanical change with no associated dev scope (a standalone docs edit, a skill-file fix, a one-line config tweak) — those may be committed without asking. When in doubt about whether something counts as "dev scope," ask.
- **Push confirmation**: this is a separate question from the commit above, and only applies once a commit already exists. On a feature branch, following the `open-pr`/`finish-issue` flow, Claude pushes an *already-confirmed* commit without asking again — the plan/PR checkpoints already validated the work, so re-asking would be redundant. A direct commit on `main`/`dev` (the "commit pontual" exception, unreviewed by a PR) may still be pushed by Claude, but only after explicit user confirmation for that specific push — nothing in that commit went through a review gate, and undoing it once it's on a shared branch means a rebase. **Exception: docs-only commits** (touching only `docs/`, `CLAUDE.md`, `AGENTS.md`, `.claude/rules/`) push straight through without asking — no dev scope, nothing to review. Anything else, including a change to `.claude/settings.json` itself, still asks. Enforced by a `PreToolUse`/`Bash` hook in `.claude/settings.json` (checks the pushed diff against `origin/<branch>` when `git push` runs from `main`/`dev`).
- **Ask Draft-vs-ready at the moment the PR is created, every time — never assume, never default silently either way.** Push-without-asking (above) only covers getting commits onto the remote as a backup; it does **not** extend to making a PR mergeable. Right before running `gh pr create`, ask explicitly: "Draft ou já ready?" — DoD status (green or not) never answers this question on its own; it's the user's call every single time, in both directions. Confirmed the hard way: PR #38 (issue #24) got auto-merge armed the instant it was created with no review window at all (too eager), and later the reverse problem showed up — Claude kept defaulting every PR to Draft and making the user separately ask for ready each time (too rigid). Neither default is right; asking at creation time is. If the answer is Draft, promoting later still needs the same explicit ask (see `open-pr` step 7 and `finish-issue`).
- **"Ready" means non-draft and visible for review — it does NOT mean merge, and does NOT mean arm auto-merge.** Those are separate actions requiring their own separate, explicit instruction ("pode mergear", "arma o auto-merge"), given only after the user has actually had a chance to look at the PR on GitHub. Never call `gh pr merge` (auto or direct) in the same action as `gh pr create`/`gh pr ready`, no matter how green the DoD looks or how confidently the user answered "ready." Confirmed the hard way (#134, 2026-08-10): `gh pr create` was immediately followed by arming auto-merge in the same turn; combined with a separate bug (commits never `git push`ed before `gh pr create`, so the PR opened with an empty diff — see the `open-pr` step below), the empty-diff PR merged into the epic branch within seconds, before the user could open it and notice anything was wrong. In the user's words: "Uma coisa é um PR ready pronto para eu poder olhar. Outra coisa é um PR em ready que você já mergeou e eu nem consegui ver o PR." Auto-merge existing because CI gates it is a safety net for *after* a merge decision is made — not a reason to skip the human review window.
- **Always `git push` the branch and verify the diff before `gh pr create`.** `gh pr create` does not push commits itself — it opens a PR from whatever the remote branch already has. If local commits (e.g. from later in a session, after `createLinkedBranch` created the branch early) were never pushed, the PR can silently open empty or stale. Confirmed the hard way (#134): 12 local commits were never pushed before `open-pr` ran `gh pr create`, so the PR opened comparing the epic branch against a 12-commits-behind remote ref — a near-empty diff that then got auto-merged (see above) without anyone noticing. Run `git push origin <branch>` explicitly, then confirm with `git log <base>..HEAD --oneline` / `git diff <base>..HEAD --stat` that the diff about to go into the PR actually contains what's expected, every time — not just when something feels off.

## Conventions & rules

Coding conventions (MVI contract, ViewModel/use-case rules, testing, naming) live in **`docs/CONVENTIONS.md`**. The per-layer non-negotiables auto-load via **`.claude/rules/`**, scoped to `domain/**`, `data/**`, `feature/**` (+ `core/backup`) — so they only enter context when you touch that layer. `resume-work.md` uses the same mechanism scoped to `.claude/plans/**` — it loads the resume-work protocol the moment a plan file is opened.

## More docs

- **`docs/BLUEPRINT.md`** — original class/screen inventory + implementation backlog, frozen as planning context. Live issue/PR status lives in the "unideas — Improvements" artifact + the GitHub Project board, not here.
- **`docs/RELEASE.md`** — build variants, signing, release automation, secrets, SemVer.
- **`docs/RUNNING.md`** — running/inspecting the app (`android` CLI) + git hooks (`./gradlew installGitHooks`).
