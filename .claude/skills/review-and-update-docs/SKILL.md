---
name: review-and-update-docs
description: Use when the user asks to analyze recent changes — reviews what was improved, identifies regressions or issues, then updates CLAUDE.md, AGENTS.md, README.md and docs/ to reflect the current state.
---

# Review & Update Docs — unideas Workflow

## Usage

```
/review-and-update-docs
```

Invoke after a refactoring session, feature completion, or any time the user says "analisa o que fiz" / "valida o que mudou".

---

## Step-by-step

### 1. Identify what changed

```bash
git diff HEAD --name-only          # unstaged modifications
git status --short                 # new untracked files
git log --oneline -10              # recent commits for context
```

For each changed/new file, read its current content.

### 2. Analyze improvements

For each change, determine:
- **What improved** — cleaner code, better patterns, correct idioms, new conventions
- **What regressed** — DRY violations, dead code, inconsistencies, wrong layer responsibilities

Report findings before touching any file. Be specific: file name, line, what the issue is.

### 3. Decide if docs need updating

Docs need updating when the change:
- Adds a new package, module, or architectural concept
- Changes a dependency-injection pattern or module structure
- Adds a new code convention (naming, state shape, event handling, etc.)
- Changes the project state (feature completed, screen added, build config changed)
- Introduces a new utility or helper pattern

Docs do NOT need updating for: internal refactors, bug fixes, style-only changes.

### 4. Update docs (if needed)

Start by finding ALL `.md` files in the project:

```bash
find . -name "*.md" -not -path "./.git/*" -not -path "*/build/*" | sort
```

Read every file that could be affected. Update only the sections that are actually out of date.

Current doc set (grows as the project grows — add rows here as new docs are created):

| File | Update when |
|---|---|
| `CLAUDE.md` | Stack, commands, architecture/package layout, or project state changed |
| `AGENTS.md` | Nothing to do — it's a **symlink to `CLAUDE.md`** (single source of truth). Never convert it back to a copy. |
| `.claude/rules/*` | A per-layer coding convention changed (rules are path-scoped: `domain/**`, `data/**`, `feature/**`) |
| `README.md` | Stack table, getting-started steps, or app description changed |
| `docs/*` | Once a `docs/` folder exists: update the specific doc whose topic changed (architecture, features, testing, etc.) |
| `.claude/skills/*.md` | A skill's instructions became stale |

### 5. Report

After analysis and any updates, deliver a structured report:

```
## O que melhorou
<bulleted list of concrete improvements>

## Issues encontradas
<numbered list — only real problems, not style preferences>

## Docs atualizadas
<list of files changed and what section was updated>
  — or "Nenhuma atualização necessária" if nothing changed>
```

---

## What counts as an issue

Only flag things that have a concrete negative impact:

| Type | Example |
|---|---|
| Dead code | Declared but never used/injected |
| DRY violation | Identical composable copy-pasted in 2 files |
| Wrong layer | ViewModel using Context/Resources directly |
| Inconsistency | Some errors use `@StringRes Int`, others use raw `String` |
| Empty file | File with only a package declaration |
| Unused resource | String in strings.xml never referenced |

Do NOT flag: personal style preferences, trivial naming, or things that are correct but not your preferred pattern.

---

## Common mistakes

| Mistake | Fix |
|---|---|
| Updating docs for style-only changes | Only update when structure/state/convention changed |
| Flagging things that aren't really issues | Apply the "concrete negative impact" test |
| Rewriting doc sections that are still accurate | Edit only the stale parts |
| Missing issues because a file wasn't read | Read every changed/new file, not just the diff summary |
| Turning `AGENTS.md` back into a copy of `CLAUDE.md` | It's a symlink — edit only `CLAUDE.md` |
| Bloating `CLAUDE.md` with procedural/reference detail | Keep it lean (always-on); put procedures in `docs/` or `.claude/rules/` and link |
