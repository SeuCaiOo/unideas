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
- Assignee: always **`SeuCaiOo`** (único dev do projeto)
- Diff: compare commits only against the **target branch** (`git log dev..HEAD`), never against `main`

## Step-by-step

### 1. Branch
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
./gradlew koverVerify
```
**Must pass before opening PR.** If it fails, add missing tests first. `dev_checks.yml` (CI) enforces the same check on the PR.

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
  --assignee SeuCaiOo
```

Then apply the label:
```bash
gh pr edit <number> --add-label "<label>"
```

If the PR closes a GitHub issue, add `Closes #<issue>` to the PR body — but this project hasn't decided yet whether it will use GitHub Issues/Project board for day-to-day work (see item 9 of the bootstrap guide), so treat this as optional, not a required step.

### 7. Auto-merge (PRs targeting `main`)

`main` has branch protection requiring the `Quality Gate` check (from `main_build.yml`) to pass, and the repo has `allow_auto_merge` enabled — this only works because the repo is public (branch protection on private repos needs a paid GitHub plan). For the periodic `dev` → `main` release PR, enable auto-merge right after creating it so it merges by itself once CI passes, instead of waiting around:

```bash
gh pr merge <number> --auto --merge
```

Use `--merge` (merge commit) to match the existing convention (see PR #2). This is **not** needed for feature → `dev` PRs — `dev` has no branch protection yet.

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