---
name: finish-issue
description: Validates DoD against what was implemented and updates issue checkboxes — run BEFORE a PR is created ready-for-review (or before promoting an already-open Draft PR). This is the gate that decides whether a PR is allowed to leave Draft state / get auto-merge enabled.
---

# Finish Issue — unideas Workflow

## When to run this

DoD validation is a **pre-merge gate**, not post-merge bookkeeping — it must happen before a PR becomes mergeable, not after. Two entry points, same skill either way:

1. **Implementation just finished, no PR yet** → run this first. If it passes, hand off to `/open-pr`, which creates the PR already as ready-for-review + auto-merge (no need to open a Draft first).
2. **A Draft PR is already open** (opened early for CI feedback while still coding) → run this once you believe the work is complete. If it passes, promote the PR: `gh pr ready <number>` + `gh pr merge <number> --auto --merge`.

**A PR must never get auto-merge enabled before this skill reports done.** If DoD isn't ready yet but you want CI feedback or visibility, open/keep the PR as **Draft** (`gh pr create --draft` — see `open-pr` step 6) — GitHub natively blocks merging a Draft, which is exactly the "can be open, can't be merged" gate this project wants, with no custom tooling needed.

---

## Step-by-step

### 1. Fetch issue (and PR, if one already exists)
```bash
gh issue view <issue-number> --json number,title,body,state
gh pr view <pr-number> --json number,title,url,headRefName,isDraft   # only if a PR already exists
```

### 2. Reconcile DoD against the real diff

Compare the issue's Checklist/DoD section against `git log dev..HEAD` / `git diff dev..HEAD`. Every item lands in one of three buckets:

- **Done as written** → will be checked `[x]` in step 3.
- **Not done** → STOP (see below), don't open/promote the PR yet.
- **Scope changed** — implementation did more, less, or something different than the item's original wording describes. **Do not silently rewrite the checklist.** Report the discrepancy to the user (original wording vs. what was actually built) and wait for explicit confirmation before editing the issue body — this follows the same "validate before permanent" rule used for issues/PRs elsewhere in this project. Only after confirmation, reword the item to match reality, then check it off.

**If any item is genuinely not done → STOP and report:**
```
⚠️ DoD incompleto para a issue #N.

Itens não concluídos:
  - [ ] Testes unitários escritos e passando

Conclua os itens antes de abrir/promover o PR.
```

**Only proceed to step 3 once every item is either checked or reconciled-and-checked.**

### 3. Update issue checkboxes

```bash
gh issue view <issue-number> --json body --jq '.body' > /tmp/issue_body.md
# check completed items; reword any reconciled items per step 2 (only after user confirmation)
gh issue edit <issue-number> --body-file /tmp/issue_body.md
```

### 4. Gate the PR

- **No PR yet** → hand off to `/open-pr`; it creates the PR directly as ready-for-review with auto-merge enabled (since DoD is already green) and syncs the Improvements artifact in the same pass (`open-pr` step 6.5).
- **Draft PR already open** → promote it now, then sync the Improvements artifact (same mechanics as `open-pr` step 6.5 — it was skipped at Draft-creation time since DoD wasn't green yet):
```bash
gh pr ready <pr-number>
gh pr merge <pr-number> --auto --merge
```

### 5. Report
```
✅ DoD validado para a issue #N — pronto para PR/merge.
```

**Note:** the unideas board has `Backlog` / `Todo` / `In Progress` / `Done` / `Released` (no `In Review`) — the card stays in "In Progress" here, even with DoD green and the PR mergeable. The sweep to "Done" (closing the issue, moving the card, syncing the parent epic and the **"unideas — Improvements"** artifact) happens later, once the PR has actually merged into `dev`, on the next `/start-feature` run — that's a fact-check against reality (did it merge?), not a self-assessment, so it's kept separate from this skill. `Released` is a further, later step tied to an actual shipped version.

---

## Common mistakes

| Mistake | Fix |
|---|---|
| Validating DoD after the PR already merged | Validate before the PR is created ready (or before a Draft is promoted) — this is a pre-merge gate, not post-merge bookkeeping |
| Marking DoD done without checking commits | Confirm each item against the real diff (`git log dev..HEAD`) |
| Rewriting checklist wording without asking | Scope drift must be confirmed with the user before the issue body changes |
| Enabling auto-merge before DoD passes | Never run `gh pr merge --auto` (or `gh pr ready`) until this skill reports done |
| Treating "DoD green" as "move card to Done" | Card movement waits for the actual merge, checked by `/start-feature`'s next run — not by this skill |
