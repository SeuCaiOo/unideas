---
name: start-feature
description: Use when starting development of a GitHub issue — first moves any closed issues/PRs to Done (syncing parent epics and the Improvements artifact too), then validates DoR, creates branch, generates and saves a development plan, moves issue to In Progress, pulls the rest of its lettered backlog group into Todo, promotes its parent epic if any, syncs the Improvements artifact, then enters planning mode.
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

**Parent epic sync**: for each issue just closed above, check whether it has a parent via GitHub's native sub-issues relationship:

```bash
gh api graphql -f query="
{
  repository(owner: \"SeuCaiOo\", name: \"unideas\") {
    issue(number: <N>) {
      parent {
        number
        title
        subIssuesSummary { total completed }
      }
    }
  }
}"
```

If `parent` is non-null:
- If `subIssuesSummary.completed == subIssuesSummary.total` (every sub-issue of the epic is now done), close the parent issue too (same comment pattern as step 1 above, referencing which sub-issue completed the set) and move its card to Done — same mutation as for the sub-issue, using the parent's own project item ID.
- Otherwise (some sub-issues still open), just confirm the parent's card is already `In Progress` (it should be, from `start-feature` step 9 when the first sub-issue started) — move it there if it somehow isn't, but do **not** close it or touch its Done status yet.

This keeps epic issues (e.g. #5 "Room persistence layer") honest about partial progress instead of sitting untouched in Backlog while their sub-issues get worked on and finished one at a time.

**Improvements artifact sync**: for each issue just closed above (and its parent, if promoted/closed per the sync above), also update the **"unideas — Improvements"** artifact — URL in `.claude/skills/add-improvement/SKILL.md` (`https://claude.ai/code/artifact/ee42af85-d23b-4c39-aa3a-ded2829a2667`). This is the same artifact `add-improvement` writes new ideas into; it doubles as the living index of the whole `v0.1.0` backlog (issues #3–#30), so it needs to move in lockstep with the board:

1. `WebFetch` the artifact URL for its current markdown — never assume its content from memory, another session may have changed it.
2. Find the entry whose heading contains `(#<N>)`. Check every `- [ ]` in its checklist to `- [x]`. Add or update a status tag right after its `pré-req` line, matching the existing convention: `· ✅ **Merged** (PR #<M> → dev, implementado via <how>)`.
3. If the issue has a parent epic: update the parent's own status tag too (`· ⏳ **In Progress** (X/Y sub-issues — ...)` or, once all sub-issues are done, `· ✅ **Merged**` — mirror whatever was just decided in the Parent epic sync above).
4. Add a one-line entry for the issue (and parent, if it just completed) under **"## Finalizadas (Done)"**; if the issue's epic is still partially open, make sure it's listed under **"## Em andamento (In Progress)"** instead (remove it from there once fully done).
5. Write the full updated markdown to a local scratchpad file and republish via the `Artifact` tool with the same `url` — never a new `file_path`-only publish, that would mint a second artifact.

See the sync done for #21/#5 in this project's history for a worked example of the exact edits.

**Remote-only cleanup, never local**: the repo has `delete_branch_on_merge` enabled, so GitHub deletes the head (feature) branch on the remote automatically once its PR merges — `main`/`dev` are never affected, since they're always the PR *base*, never the head. Nothing to do here on the remote side.

**Do NOT delete local feature branches** (`git branch -d`), even ones already merged. The user explicitly wants them kept locally as a debugging safety net — if a merged change turns out buggy later, the local branch is how you go back and see exactly what that PR's state was, without depending on GitHub still having it (it won't, remote is auto-deleted) or reconstructing it from commit SHAs. `git fetch --prune origin` (dropping stale *remote-tracking* refs, not local branches) is fine and harmless if you want to run it, but skip local `git branch -d` entirely — this was done by mistake twice in this project's history and had to be reverted both times.

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

### 3. Create branch — via `createLinkedBranch`, not plain `git checkout -b`

Branch naming pattern: `<type>/#<number>/<slug>`
- Extract `type` from the issue title prefix (e.g. `feat`, `fix`, `refactor`, `test`, `chore`)
- Extract slug from the issue title: lowercase, spaces → hyphens, remove special chars
- Example: `feat: Add login screen` → `feat/#4/add-login-screen`

Don't create the branch with a plain `git checkout -b` + later `git push` — that only links the PR to the issue much later (and unreliably) via the `Closes #N` text, which never even auto-closes anyway since PRs target `dev`, not the repo's default branch (confirmed empirically: `willCloseTarget: false` on the cross-reference event, regardless of merge). Instead, use GitHub's `createLinkedBranch` GraphQL mutation, which creates the branch **and** links it in the issue's Development section atomically, at the moment the branch is born — validated live on a throwaway issue (`linkedBranches` populated instantly, no delay, unlike `closingIssuesReferences`).

```bash
git checkout <base-branch>
git pull origin <base-branch>

ISSUE_NODE_ID=$(gh api repos/SeuCaiOo/unideas/issues/<issue-number> --jq '.node_id')
BASE_OID=$(git rev-parse HEAD)

gh api graphql -f query="
mutation {
  createLinkedBranch(input: {
    issueId: \"$ISSUE_NODE_ID\"
    oid: \"$BASE_OID\"
    name: \"<type>/#<number>/<slug>\"
  }) {
    linkedBranch { ref { name } }
  }
}"

git fetch origin "<type>/#<number>/<slug>"
git checkout "<type>/#<number>/<slug>"
```

This pushes the branch to the remote immediately too — a real backup from the first commit, not just at PR time. Read the mutation's response `linkedBranch.ref.name` to confirm the actual created branch name (GitHub could in principle sanitize it) before the `git fetch`/`checkout`.

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

### 8. Promote the rest of this letter group to "Todo"

The `docs/BLUEPRINT.md` / Improvements-artifact backlog is organized into lettered groups (`A · Fundação de dados`, `B · Casos de uso`, `C · Design system`, ...). `Backlog` means "everything specced, no timeline"; `Todo` means "queued up next, no more thinking needed to know what's coming." The user wants the **whole group** pulled into `Todo` together as soon as the first issue of that group starts — not one issue at a time, since seeing 2-3 loose `Todo` cards while the rest of the group sits in `Backlog` defeats the point (you'd still have to ask "what's next?").

Determine the group: `WebFetch` the Improvements artifact (URL in `.claude/skills/add-improvement/SKILL.md`) and find the `### <Letter> · <name>` heading containing this issue's `(#<N>)`. Collect every issue number under that heading (top-level items and their `↳` sub-issues) up to the next `### ` heading.

For every issue in that list that is **not** the one just moved to In Progress in step 7 and whose board status is currently `Backlog`, move it to `Todo` (option ID `f75ad846`) — same mutation pattern as step 7, just swap the target status. Leave anything already `Todo`/`In Progress`/`Done` untouched. Report the full list of what got pulled forward.

Example: starting #21 (group A) should have pulled #22 (still A, the only sibling not yet started) into `Todo` at the same time — it didn't, because this step didn't exist yet; done manually once, now automated going forward.

### 9. Promote parent epic issue (if this is a sub-issue)

Check whether the issue has a parent, using GitHub's native sub-issues relationship (not text parsing — unreliable, since sub-issue bodies don't consistently spell out "#<parent-number>"):

```bash
gh api graphql -f query="
{
  repository(owner: \"SeuCaiOo\", name: \"unideas\") {
    issue(number: <issue-number>) {
      parent { number title }
    }
  }
}"
```

If `parent` is non-null, find the parent's project item and current status the same way as step 7 (swap in the parent's issue number). If the parent's status is `Backlog` or `Todo`, move it to `In Progress` too — starting work on any sub-issue means the epic itself is now in progress, even though it isn't finished. If the parent is already `In Progress` (a later sibling sub-issue), leave it as-is. Report which parent (if any) was promoted, and to what status it was found before promoting.

### 10. Sync the Improvements artifact (mark as started)

Same artifact as referenced in step 0 — `.claude/skills/add-improvement/SKILL.md` has the URL. `WebFetch` its current content, find the entry for this issue (`(#<issue-number>)` in the heading). This step is about visibility, not completion, so keep it light: no status tag change is required for an in-progress item (the artifact's convention only tags `✅ Merged`/`⏳ In Progress` on *epics*, not individual sub-issues mid-flight) — but if this issue **is** an epic itself (has its own sub-issues) or the parent promoted in step 9, add/update its `⏳ In Progress` status tag now, same format as the Done-time tag in step 0. Republish with the same `url`. Skip silently if nothing needs to change (e.g. this is a plain leaf issue with no epic-level tag to add).

### 11. Enter planning mode

Summarize what was set up:
- ✅ DoR validated
- ✅ Branch created: `<branch-name>`
- ✅ Plan saved: `<plan-path>`
- ✅ Issue moved to "In Progress"
- ✅ Rest of the letter group pulled into "Todo"
- ✅ Parent epic promoted to "In Progress" (if applicable)
- ✅ Improvements artifact synced

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
| Leaving the rest of a lettered group sitting in Backlog while one issue is worked | Always run step 8 — pull every sibling issue of the same group into Todo, not just the one being started |
| Leaving a parent epic stuck in Backlog while its sub-issues progress | Always run step 9 (starting) and the Parent epic sync in step 0 (finishing) — use the native `parent`/`subIssuesSummary` GraphQL fields, never guess the parent number from body text |
| Closing a parent epic while sibling sub-issues are still open | Only close the parent when `subIssuesSummary.completed == total` — otherwise just confirm it's `In Progress` |
| Forgetting to sync the Improvements artifact | Always run step 0's artifact sync (finishing) and step 10 (starting) — it's the same URL `add-improvement` writes to, don't wait for the user to paste the link |
| Creating the branch with plain `git checkout -b` for an issue-tied feature | Always use `createLinkedBranch` (step 3) instead — plain branch creation + a later `Closes #N` in the PR body does NOT reliably link the issue's Development section for `dev`-targeting PRs (confirmed empirically, #22/#35) |