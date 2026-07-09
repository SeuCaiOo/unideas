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

This links the PR to the issue on GitHub — merging it will close the issue automatically.

### 5. Report

```
✅ Issue #N finalizada:

- ✅ DoD validado — todos os itens concluídos
- ✅ Checkboxes atualizados na issue
- ✅ PR #M vinculado à issue

PR: <pr-url>
```

**Note:** the unideas board has `Backlog` / `Todo` / `In Progress` / `Done` / `Released` (no `In Review`) — the card is deliberately left in "In Progress" here. It gets swept to "Done" automatically the next time `/start-feature` runs (step 0), once the issue is closed by the merge.

---

## Common mistakes

| Mistake | Fix |
|---|---|
| Marking DoD done without checking commits | Confirm each item against the real diff (`git log dev..HEAD`) |
| Skipping checkbox update | Always update the issue — makes future audits easier |
| Not linking PR to issue | `Closes #N` in the PR body is required — it drives the auto-close on merge |
| Trying to move the card to "In Review" | That column doesn't exist on this board; leave it in "In Progress" |