---
name: start-feature
description: Use when starting development of a GitHub issue — first moves any closed issues/PRs to Done, then validates DoR, creates branch, generates and saves a development plan, moves issue to In Progress, then enters planning mode.
---

# Start Feature — unideas Workflow

## Usage

```
/start-feature #<issue-number>
```

---

## Step-by-step

### -1. Determine base branch and review docs

First, find out which branch the new feature branch should be created from:

```bash
git branch --show-current
```

**Ask the user:** "A branch base para criar a nova branch é `<current-branch>`? Ou deve ser outra?"

Wait for confirmation before proceeding. Do NOT assume `dev`. Do NOT switch branches without explicit user authorization.

Once the base branch is confirmed, check if it has commits that aren't yet reflected in the docs:

```bash
git log origin/<base-branch> --oneline -10
```

Look for recent merges (PRs merged into the base branch) since the last `docs:` commit. If there are feature commits not covered by a `docs:` commit after them, invoke `/review-and-update-docs` now.

**Why here:** The base branch already contains the previous feature at this point. This is the earliest moment where the docs can be updated with full context — before the new branch is created.

If docs are already up to date (last commit is a `docs:` commit, or no meaningful `.kt` changes since last docs update), skip this step and move on.

### 0. Move merged-into-dev issues to "Done"

The unideas project board has two staging levels before work starts — `Backlog` (everything captured/specced) and `Todo` (the prioritized subset, promoted manually) — then `In Progress`, `Done`, and `Released`. No `In Review`. `finish-issue` deliberately doesn't move the card, so this step is where stale cards get swept to Done.

**Done criterion: the issue's PR merged into `dev`** — NOT "the GitHub issue is closed". Feature PRs always target `dev`, never the repo's default branch (`main`), so GitHub's `Closes #N` auto-close never fires on merge — waiting for `state:closed` would leave cards stuck in "In Progress" forever. `Released` is the separate, later step for "shipped in an actual generated version" (`0.0.x`/`0.1.0`, moved manually at release time) — don't conflate the two.

Check for merged PRs (into `dev`) referencing an issue not yet in "Done":

```bash
gh pr list --base dev --state merged --json number,title,body,mergedAt --limit 30
```

For each merged PR, extract the issue number from its `Closes #<N>` line in the body. For each such issue:

```bash
# 1. Close the issue now if still open (don't wait for the dev->main release)
gh issue close <N> --comment "Mergeado em \`dev\` via #<PR>. Fecho aqui — o board rastreia \"já está numa versão gerada\" separadamente, na coluna Released."

# 2. Move its board card to Done
ISSUE_NODE_ID=$(gh api repos/SeuCaiOo/unideas/issues/<N> --jq '.node_id')
ITEM_ID=$(gh api graphql -f query="
{ node(id: \"$ISSUE_NODE_ID\") { ... on Issue { projectItems(first: 5) { nodes { id } } } } }" --jq '.data.node.projectItems.nodes[0].id')
gh api graphql -f query="
mutation {
  updateProjectV2ItemFieldValue(input: {
    projectId: \"PVT_kwHOAVNuW84Bcrp8\"
    itemId: \"$ITEM_ID\"
    fieldId: \"PVTSSF_lAHOAVNuW84Bcrp8zhXSou4\"
    value: { singleSelectOptionId: \"98236657\" }
  }) { projectV2Item { id } }
}"
```

Skip issues already closed (already handled by a previous run).

Known field/option IDs (project `PVT_kwHOAVNuW84Bcrp8`, https://github.com/users/SeuCaiOo/projects/4):
- Status field ID: `PVTSSF_lAHOAVNuW84Bcrp8zhXSou4`
- Backlog option ID: `19386e88`
- Todo option ID: `f75ad846`
- In Progress option ID: `47fc9ee4`
- Done option ID: `98236657`
- Released option ID: `edd7e261`

Report what was moved (and which issues closed) before continuing.

### 1. Fetch issue data

```bash
gh issue view <number> --json number,title,body,labels
```

### 2. Validate DoR

Parse the **Definition of Ready** section from the issue body.
Look for checkboxes in the format `- [ ]` (unchecked) vs `- [x]` (checked).

**If any DoR item is unchecked → STOP.**
Show which items are missing and ask the user to complete them before proceeding:

```
❌ Issue #N não está pronta para desenvolvimento.

DoR incompleto:
  - [ ] Pré-requisitos concluídos
  - [ ] Checklist de implementação definido

Complete os itens acima na issue antes de iniciar.
```

**Only proceed if ALL DoR items are checked.**

### 3. Create branch

Branch naming pattern: `<type>/#<number>/<slug>`
- Extract `type` from the issue title prefix (e.g. `feat`, `fix`, `refactor`, `test`, `chore`)
- Extract slug from the issue title: lowercase, spaces → hyphens, remove special chars
- Example: `feat: Add login screen` → `feat/#4/add-login-screen`

```bash
git checkout <base-branch>
git pull origin <base-branch>
git checkout -b <type>/#<number>/<slug>
```

### 4. Load project conventions

Before planning, read `CLAUDE.md` and `AGENTS.md` at the repo root to ground the plan in actual project conventions (namespace, module layout under `ui/`, Detekt/Kover setup, commit and branch rules). Do not skip this step — the plan's file paths and package structure must match what's already documented, not be inferred from memory. As the codebase grows and per-feature docs appear under `docs/`, read those too when relevant.

### 5. Generate development plan

Build a structured plan based on the issue body AND the conventions loaded above. The plan must include:

```markdown
# Plan: <issue title>

## Context
<issue context section>

**Pré-requisito:** <from issue>

---

## Critical files
<identify files to create/modify based on checklist>

---

## Checklist
<mirror the implementation checklist from the issue, grouped by layer if applicable>

---

## Verification
<mirror the DoD from the issue>
```

### 6. Save plan

Save the plan to `.claude/plans/` (repo-relative, gitignored) using the branch slug as filename:

```
.claude/plans/<type>-#<number>-<slug>.md
```

Example: `.claude/plans/feat-#4-add-login-screen.md`

### 7. Move issue to "In Progress"

Get the issue node ID, find the project item, and update the Status field to "In Progress":

```bash
ISSUE_NODE_ID=$(gh api repos/SeuCaiOo/unideas/issues/<issue-number> --jq '.node_id')

ITEM_ID=$(gh api graphql -f query="
{
  node(id: \"$ISSUE_NODE_ID\") {
    ... on Issue {
      projectItems(first: 5) {
        nodes { id }
      }
    }
  }
}" --jq '.data.node.projectItems.nodes[0].id')

gh api graphql -f query="
mutation {
  updateProjectV2ItemFieldValue(input: {
    projectId: \"PVT_kwHOAVNuW84Bcrp8\"
    itemId: \"$ITEM_ID\"
    fieldId: \"PVTSSF_lAHOAVNuW84Bcrp8zhXSou4\"
    value: { singleSelectOptionId: \"47fc9ee4\" }
  }) { projectV2Item { id } }
}"
```

### 8. Enter planning mode

Summarize what was set up:
- ✅ DoR validated
- ✅ Branch created: `<branch-name>`
- ✅ Plan saved: `<plan-path>`
- ✅ Issue moved to "In Progress"

Then present the plan to the user and ask for confirmation before starting implementation.

---

## Common mistakes

| Mistake | Fix |
|---|---|
| Starting with unchecked DoR | Always validate all DoR items first |
| Assuming base branch without asking | Always ask user to confirm base branch in step -1 |
| Plan not saved | Always write to `.claude/plans/` |
| Slug with uppercase or special chars | Normalize: lowercase, hyphens only |
| Planning without reading CLAUDE.md/AGENTS.md | Always run step 4 — never infer package structure from memory |
| Not moving issue to "In Progress" | Always run step 7 — move card before starting implementation |