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

**Must pass before opening PR.** If it fails after a clean run, add missing tests first. `dev_checks.yml` (CI) enforces the same check on the PR.

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

```bash
gh pr create \
  --base dev \
  --head <branch> \
  --title "<EN title>" \
  --body "$(cat .github/PULL_REQUEST_TEMPLATE.md)" \
  --assignee "@me"
```

Then apply the label:
```bash
gh pr edit <number> --add-label "<label>"
```

Always include `Closes #<issue>` in the PR body at creation time (in the `gh pr create --body` call). Two separate things happen from this text, and it's worth not conflating them:

- **`closingIssuesReferences` / auto-close on merge**: stays empty and merging won't auto-close the issue, because `dev` isn't the repo's default branch (`willCloseTarget: false`). This is expected — final closing happens at the `dev`→`main` release PR, or manually.
- **The "Development" sidebar link on the issue** (a `ConnectedEvent` in the issue's timeline): this **does** appear from the same `Closes #N` text, confirmed live on issue #23 / PR #37 — it showed up within a few minutes of the PR being opened/pushed. No extra step needed. There is no public API to manually link an already-open PR to an issue (checked the GraphQL schema — only `createLinkedBranch`/`deleteLinkedBranch` exist, nothing PR-equivalent); the web UI's "Link a pull request" search box has no exposed mutation, so don't try to build a workaround for it.

If the sidebar link seems missing right after opening the PR, wait a few minutes before assuming it failed — it's a webhook/indexing delay, not a broken mechanism. The branch itself is separately linked at creation time via `createLinkedBranch` in `/start-feature` step 3.

### 7. Auto-merge (PRs targeting `dev`)

`dev` has branch protection requiring the `Quality Gate` check (from `dev_checks.yml`) to pass, and the repo has `allow_auto_merge` enabled — this only works because the repo is public (branch protection on private repos needs a paid GitHub plan). Enable auto-merge right after creating a feature → `dev` PR so it merges by itself once CI passes, instead of waiting around:

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