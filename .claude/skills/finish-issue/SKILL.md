---
name: finish-issue
description: Use after opening a PR — validates DoD against what was implemented, updates issue checkboxes, and links the PR to the issue.
---

# Finish Issue — unideas Workflow

## Usage

```
/finish-issue #<issue-number> #<pr-number>
```

Invoke after `/open-pr`, once the PR is open and all work is committed.

---

## Step-by-step

### 1. Fetch issue and PR data

```bash
gh issue view <issue-number> --json number,title,body,state
gh pr view <pr-number> --json number,title,url,headRefName
```

### 2. Validate DoD

Parse the **Definition of Done** section from the issue body.
Look for checkboxes: `- [ ]` (unchecked) vs `- [x]` (checked).

For each DoD item, verify if it was actually completed based on the commits and PR content.

**If any DoD item cannot be confirmed as done → STOP and report:**

```
⚠️ DoD incompleto para a issue #N.

Itens não concluídos:
  - [ ] Testes unitários escritos e passando

Conclua os itens antes de finalizar.
```

**Only proceed if ALL DoD items are done.**

### 3. Update issue checkboxes

Mark all Checklist and DoD items as checked in the issue body:

```bash
gh issue view <issue-number> --json body --jq '.body' > /tmp/issue_body.md
# Replace - [ ] with - [x] for completed items
gh issue edit <issue-number> --body-file /tmp/issue_body.md
```

### 4. Link PR to issue

Add `Closes #<issue-number>` to the PR body via `gh pr edit`:

```bash
gh pr edit <pr-number> --body "$(gh pr view <pr-number> --json body --jq '.body')

Closes #<issue-number>"
```

This links the PR to the issue on GitHub for traceability. Note: it will **not** auto-close the issue on merge — `Closes #N` only auto-closes when merging into the repo's default branch (`main`), and this PR targets `dev`. `start-feature` step 0 closes the issue explicitly once the PR merges into `dev`.

### 5. Report

```
✅ Issue #N finalizada:

- ✅ DoD validado — todos os itens concluídos
- ✅ Checkboxes atualizados na issue
- ✅ PR #M vinculado à issue

PR: <pr-url>
```

**Note:** the unideas board has `Backlog` / `Todo` / `In Progress` / `Done` / `Released` (no `In Review`) — the card is deliberately left in "In Progress" here. Once the PR merges into `dev`, `start-feature` step 0 closes the issue, sweeps its card to "Done", syncs the parent epic (if any), and syncs the **"unideas — Improvements"** artifact (URL in `.claude/skills/add-improvement/SKILL.md`) — all in the same pass, next time `/start-feature` runs. `finish-issue` itself doesn't touch the artifact; that's deliberately deferred to merge time. `Released` is a separate, later step for when the work ships in an actual generated version.

---

## Common mistakes

| Mistake | Fix |
|---|---|
| Marking DoD done without checking commits | Confirm each item against the real diff (`git log dev..HEAD`) |
| Skipping checkbox update | Always update the issue — makes future audits easier |
| Not linking PR to issue | `Closes #N` in the PR body is required for traceability — it does NOT auto-close on merge (dev isn't the default branch); `start-feature` step 0 closes it explicitly |
| Trying to move the card to "In Review" | That column doesn't exist on this board; leave it in "In Progress" |