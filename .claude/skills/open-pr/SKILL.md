---
name: open-pr
description: Use when opening a pull request in the unideas project — covers branch setup, commit validation, label selection, PR creation targeting the correct base branch, and diff comparison scoped to the target branch only.
---

# Open PR — unideas Workflow

## Rules

- **Commits**: English, Conventional Commits format
- PR description body: **PT-BR**
- PR title: **English**, `type: short description` (add `#number` prefix only if the work is tied to a GitHub issue: `type: #number short description`)
- PR target: **`dev`**, never `main` directly. `main` only receives PRs from `dev` (release), not from feature branches.
- Assignee: whoever opens the PR (`gh pr create --assignee "@me"`) — don't hardcode a username, since another dev may work on this project in the future
- Diff: compare commits only against the **target branch** (`git log dev..HEAD`), never against `main`

## Step-by-step

### 1. Branch

**If the work is tied to a numbered GitHub issue, this step already happened in `/start-feature` (which links the branch to the issue via `createLinkedBranch` at creation time) — don't recreate it here.** Only use the plain flow below for work with no issue number (ad-hoc chore/fix, no `/start-feature` involved):

```bash
git checkout dev
git pull origin dev
git checkout -b <type>/short-description
```
Types: `feat/`, `fix/`, `chore/`, `refactor/`, `docs/`, `test/`, `ci/`

### 2. Commit

No hook local ainda aplica isso automaticamente (git hooks continuam pendentes — ver guia de bootstrap). Até lá, seguir manualmente:
- Conventional Commits, mensagem em inglês
- Sem `git commit` automático sem pedir confirmação, salvo mudanças puramente mecânicas (docs avulsas, chores) sem escopo de desenvolvimento associado

### 3. Verify test coverage
```bash
./gradlew clean
./gradlew koverVerify
```
**Always `clean` first** — stale build/configuration cache in this multi-module setup can report a coverage number that doesn't match reality (confirmed: a "failing" 31% turned out to be cache staleness, real number was well above minimum post-clean). The project is small; the extra seconds are cheap insurance against chasing a phantom failure.

**Must pass before the PR is promoted to ready** (the PR itself always opens as Draft regardless — see step 6). If it fails after a clean run, add missing tests first. `dev_checks.yml` (CI) enforces the same check on the PR.

### 3.5. Validate DoD

Run the `finish-issue` skill now, **before** creating the PR, if the issue is tied to a numbered GitHub issue. It reconciles the issue's DoD/Checklist against the real diff and updates the checkboxes. This is a pre-merge gate, not post-merge bookkeeping — don't defer it to after the PR exists.

**DoD status does not decide Draft vs. ready — it never has that authority.** Whether DoD is green or not, the PR is created as Draft in step 6 regardless (see that step for why). DoD passing only means the work itself is done; it says nothing about whether the user has looked at it yet.

No linked issue (ad-hoc chore/fix) → skip this step, there's no DoD to validate.

### 4. Check what's in this PR
```bash
# Use dev as base, não main
git log dev..HEAD --oneline
git diff dev..HEAD --stat
```

### 5. Label mapping

| Commit type | Label to apply |
|---|---|
| `feat` | `feature` |
| `fix` | `fix` or `bug` |
| `chore`, `build` | `chore` or `configuration` |
| `ci` | `tooling` |
| `docs` | `documentation` |
| `test` | `testing` |
| `style`, `refactor`, `perf` | `quality` |
| `ui` | `ui` |

Apply the label on GitHub before requesting review.

### 6. Open PR — always as Draft, no exception

**Every PR opens as Draft, regardless of DoD status.** This is not Claude's call to make either way — confirmed explicitly by the user after PR #38 (issue #24) got auto-merge armed the moment it was created, leaving no review window: "o que define se ele tá draft ou ready? Eu decido." Draft is the only thing standing between a fast CI run and an unattended merge the user never saw — if something bad got pushed to a Draft, there's still time to fix it before it can merge at all; on a ready PR with auto-merge, there might not be.

```bash
gh pr create \
  --base dev \
  --head <branch> \
  --title "<EN title>" \
  --body "$(cat .github/PULL_REQUEST_TEMPLATE.md)" \
  --assignee "@me" \
  --draft
```

Report that the PR is open as Draft and ask the user whether to promote it to ready (see step 7) — don't promote it yourself just because DoD passed in step 3.5.

Then apply the label:
```bash
gh pr edit <number> --add-label "<label>"
```

Always include `Closes #<issue>` in the PR body at creation time (in the `gh pr create --body` call). Two separate things happen from this text, and it's worth not conflating them:

- **`closingIssuesReferences` / auto-close on merge**: stays empty and merging won't auto-close the issue, because `dev` isn't the repo's default branch (`willCloseTarget: false`). This is expected — final closing happens at the `dev`→`main` release PR, or manually.
- **The "Development" sidebar link on the issue** (a `ConnectedEvent` in the issue's timeline): this **does** appear from the same `Closes #N` text, confirmed live on issue #23 / PR #37 — it showed up within a few minutes of the PR being opened/pushed. No extra step needed. There is no public API to manually link an already-open PR to an issue (checked the GraphQL schema — only `createLinkedBranch`/`deleteLinkedBranch` exist, nothing PR-equivalent); the web UI's "Link a pull request" search box has no exposed mutation, so don't try to build a workaround for it.

If the sidebar link seems missing right after opening the PR, wait a few minutes before assuming it failed — it's a webhook/indexing delay, not a broken mechanism. The branch itself is separately linked at creation time via `createLinkedBranch` in `/start-feature` step 3.

### 7. Promoting to ready + auto-merge — **the user's call, always, no exception**

The PR was opened as Draft in step 6 no matter what. Moving it to ready-for-review, arming auto-merge, and syncing the Improvements artifact all happen **together, at the same moment, only once the user explicitly says the PR is good to go** — not at DoD-green time, not right after opening. DoD passing (step 3.5) is Claude's own self-check that the checklist matches the diff; it is not the user having looked at the code, and it says nothing about whether they'll ask for changes before agreeing to ship it.

**Ask, every single time, regardless of how clean the work looks:** "PR aberta como Draft, DoD validado — quer que eu marque como ready e habilite auto-merge, ou prefere olhar o código primeiro?" While the PR sits in Draft, the user may come back with more changes ("melhora isso, ajusta aquilo") — those land as ordinary follow-up commits on the same branch, DoD gets re-validated if needed, and the same question gets asked again later. Only on an explicit yes to *that* question do all three of the following happen, in this order:

```bash
gh pr ready <number>
gh pr merge <number> --auto --merge
```

Then sync the **"unideas — Improvements"** artifact (URL in `.claude/skills/add-improvement/SKILL.md`) for this issue, right now, in the same pass — don't wait for the PR to actually merge or for the user to pull the next issue:
1. `WebFetch` the artifact URL for its current markdown.
2. Find the entry whose heading contains `(#<issue-number>)`. Check every `- [ ]` in its checklist to `- [x]`.
3. Add/update its status tag: `· ✅ **Merged** (PR #<number> → dev, implementado via <how>)` — matching the existing convention, right after its `pré-req` line.
4. If the issue has a parent epic: update the parent's status tag too, and move it (and this issue) into **"## Finalizadas (Done)"** if this was the epic's last remaining sub-issue; otherwise keep the parent listed under "Em andamento" with the updated sub-issue count.
5. Write the full updated markdown to a local scratchpad file and republish via `Artifact` with the same `url` — never a new `file_path`-only publish.

**Why the artifact sync waits for this exact moment, not DoD-green time:** DoD green only means Claude's self-check passed — the user might still ask for changes while the PR sits in Draft. If the artifact were marked done at DoD-green time and the user then requested edits before agreeing to promote, the artifact would read "done" while the work was still actually in flux. The moment the user says "pode ficar ready" is the only point that's actually final. This is *also* why the sync shouldn't wait for the next `/start-feature` run either (the old timing) — that could leave it stale for however long the user takes to start something new, especially since auto-merge now runs unattended once armed. `start-feature` step 0 keeps a fallback pass for anything that slips through (skip any issue whose artifact entry already shows `✅ Merged` with the right PR number).

`dev` has branch protection requiring the `Quality Gate` check (from `dev_checks.yml`) to pass, and the repo has `allow_auto_merge` enabled — this only works because the repo is public (branch protection on private repos needs a paid GitHub plan). `main` is **deliberately not** auto-merged: it has no branch protection and merges there (the periodic `dev` → `main` release PR) are manual, matching a stricter review since `main_build.yml` runs the full signed release build. Don't run `gh pr merge --auto` on a PR targeting `main`.

## PR Template sections (fill in PT-BR)

- **Descrição**: o que foi feito e por quê
- **Contexto adicional**: prints, links, referências (opcional)

## Common mistakes

| Mistake | Fix |
|---|---|
| Comparing diff against `main` | Use `git log dev..HEAD` |
| Label not applied | Run `gh pr edit <n> --add-label` after creating |
| PR targeting `main` directly | Feature branches always target `dev` |
| Commit message in PT-BR | Must be in English |
| Push to `main`/`dev` directly | No hook blocks this yet (item 1 of the bootstrap guide is pending) — follow this manually until git hooks are ported |
| Running `gh pr merge --auto` on a `main`-targeting PR | Auto-merge is only for `dev`; `main` merges are manual and reviewed |
| Validating DoD after the PR is already open/mergeable | Run `finish-issue` (step 3.5) before opening the PR — DoD is a pre-merge gate, not something to check after the fact |
| Opening a PR ready-for-review because DoD is green | Never — every PR opens as Draft (step 6), no exception for DoD status |
| Running `gh pr ready`/`gh pr merge --auto` without asking, for any reason including "DoD passed" | Always ask first (step 7) — this is the one rule with zero exceptions in this whole skill |
| Syncing the Improvements artifact as soon as DoD passes, while the PR is still Draft | Wait for the user's explicit go-ahead to promote (step 7) — DoD green doesn't mean they won't ask for changes first |
| Waiting for the next `/start-feature` to sync the Improvements artifact | Sync it right when the user says "pode ficar ready" (step 7), not before and not at the next `/start-feature` |