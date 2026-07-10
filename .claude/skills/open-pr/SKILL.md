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

**Must pass before the PR is opened ready-for-review** (Draft is fine either way — see step 3.5). If it fails after a clean run, add missing tests first. `dev_checks.yml` (CI) enforces the same check on the PR.

### 3.5. Validate DoD — decides Draft vs. ready

Run the `finish-issue` skill now, **before** creating the PR, if the issue is tied to a numbered GitHub issue. It reconciles the issue's DoD/Checklist against the real diff and updates the checkboxes. This is a pre-merge gate, not post-merge bookkeeping — don't defer it to after the PR exists.

- **DoD fully green** → proceed to step 6 and open the PR as ready-for-review with auto-merge enabled directly.
- **DoD not yet green** (e.g. opening early for CI feedback on a long task, or something is still genuinely missing) → open the PR as **Draft** in step 6, skip auto-merge, and come back to `finish-issue` later to promote it (`gh pr ready` + `gh pr merge --auto --merge`) once DoD passes.

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

### 6. Open PR

DoD green (step 3.5) → open ready-for-review:
```bash
gh pr create \
  --base dev \
  --head <branch> \
  --title "<EN title>" \
  --body "$(cat .github/PULL_REQUEST_TEMPLATE.md)" \
  --assignee "@me"
```

DoD not yet green → open as **Draft** instead (same command plus `--draft`), and skip step 7 (auto-merge) until `finish-issue` promotes it later:
```bash
gh pr create \
  --base dev \
  --head <branch> \
  --title "<EN title>" \
  --body "$(cat .github/PULL_REQUEST_TEMPLATE.md)" \
  --assignee "@me" \
  --draft
```

Then apply the label:
```bash
gh pr edit <number> --add-label "<label>"
```

Always include `Closes #<issue>` in the PR body at creation time (in the `gh pr create --body` call). Two separate things happen from this text, and it's worth not conflating them:

- **`closingIssuesReferences` / auto-close on merge**: stays empty and merging won't auto-close the issue, because `dev` isn't the repo's default branch (`willCloseTarget: false`). This is expected — final closing happens at the `dev`→`main` release PR, or manually.
- **The "Development" sidebar link on the issue** (a `ConnectedEvent` in the issue's timeline): this **does** appear from the same `Closes #N` text, confirmed live on issue #23 / PR #37 — it showed up within a few minutes of the PR being opened/pushed. No extra step needed. There is no public API to manually link an already-open PR to an issue (checked the GraphQL schema — only `createLinkedBranch`/`deleteLinkedBranch` exist, nothing PR-equivalent); the web UI's "Link a pull request" search box has no exposed mutation, so don't try to build a workaround for it.

If the sidebar link seems missing right after opening the PR, wait a few minutes before assuming it failed — it's a webhook/indexing delay, not a broken mechanism. The branch itself is separately linked at creation time via `createLinkedBranch` in `/start-feature` step 3.

### 7. Auto-merge (PRs targeting `dev`, DoD already green) — **ask first**

**Skip this step entirely if the PR was opened as Draft in step 6** — GitHub refuses to merge a Draft regardless, and enabling auto-merge on one just means it fires the moment someone marks it ready, bypassing the DoD gate. Come back to it via `finish-issue` once DoD passes.

**Never run `gh pr merge --auto` right after creating the PR without asking the user first.** DoD passing is Claude's own self-check that the work matches the checklist — it is not the user having looked at the code. Confirmed the hard way (issue #24 / PR #38): Claude opened the PR and armed auto-merge in the same breath, leaving the user zero window to review before it could merge on its own — caught only because CI happened to still be running. Push-without-asking (this skill, step 2 onward) covers getting commits onto the remote as a backup; it does **not** extend to arming the PR to merge unattended. Ask something like "PR aberta, DoD validado — posso habilitar auto-merge, ou você quer olhar o código primeiro?" and wait for a yes before running the command below.

`dev` has branch protection requiring the `Quality Gate` check (from `dev_checks.yml`) to pass, and the repo has `allow_auto_merge` enabled — this only works because the repo is public (branch protection on private repos needs a paid GitHub plan). Once the user confirms:

```bash
gh pr merge <number> --auto --merge
```

Use `--merge` (merge commit) to match the existing convention.

`main` is **deliberately not** auto-merged: it has no branch protection and merges there (the periodic `dev` → `main` release PR) are manual, matching a stricter review since `main_build.yml` runs the full signed release build. Don't run `gh pr merge --auto` on a PR targeting `main`.

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
| Validating DoD after the PR is already open/mergeable | Run `finish-issue` (step 3.5) before opening a ready PR — DoD is a pre-merge gate, not something to check after the fact |
| Enabling auto-merge on a Draft PR | Skip step 7 for Drafts; promote via `finish-issue` (`gh pr ready` + `gh pr merge --auto`) once DoD passes |
| Running `gh pr merge --auto` right after opening the PR, without asking | DoD green ≠ user reviewed the code — always ask before step 7, even on a ready (non-Draft) PR |