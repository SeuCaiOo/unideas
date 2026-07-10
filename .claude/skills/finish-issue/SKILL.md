---
name: finish-issue
description: Validates DoD against what was implemented and updates issue checkboxes — this is Claude's self-check, run before opening a PR or before asking the user whether an open Draft PR can be promoted. It never decides Draft-vs-ready itself; that's always the user's explicit call.
---

# Finish Issue — unideas Workflow

## When to run this

DoD validation is a **pre-merge gate**, not post-merge bookkeeping — it must happen before a PR becomes mergeable, not after. Two entry points, same skill either way:

1. **Implementation just finished, no PR yet** → run this first. If it passes, hand off to `/open-pr`, which opens the PR as **Draft** (always — see `open-pr` step 6, no exception for DoD status).
2. **A Draft PR is already open** (opened early for CI feedback while still coding, or just because every PR opens Draft now) → run this once you believe the work is complete.

**This skill validates whether the work is done. It never decides whether the PR gets promoted to ready or gets auto-merge armed — that is the user's call, always, asked explicitly, with zero exceptions.** DoD passing is Claude's self-check that the checklist matches the diff; it is not the user having looked at the code. Confirmed the hard way: PR #38 (issue #24) got auto-merge armed the instant it opened, leaving no review window at all — the user then made explicit that Draft vs. ready is their decision alone, not something DoD status can authorize.

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

If this issue is a parent/epic (has sub-issues), also reconcile *its own* body checklist once every sub-issue is done — an epic's checklist is its DoD too, even though it has no PR of its own and often isn't labeled "DoD" the same way a leaf issue's is. See `start-feature`'s Parent epic sync step for the mechanics; don't close an epic on `subIssuesSummary` alone without also checking off its own body.

### 3. Update issue checkboxes

```bash
gh issue view <issue-number> --json body --jq '.body' > /tmp/issue_body.md
# check completed items; reword any reconciled items per step 2 (only after user confirmation)
gh issue edit <issue-number> --body-file /tmp/issue_body.md
```

### 4. Report — and ask, don't act, on promotion

```
✅ DoD validado para a issue #N.
```

**Don't sync the Improvements artifact yet — that waits for the ready-promotion moment, not DoD passing.** DoD green only means Claude's self-check passed; the user might still ask for changes before agreeing to ship. Marking the artifact "done" now, only to have the user request edits while the PR sits in Draft, would leave it lying about the actual state.

If a PR already exists (Draft, per `open-pr` step 6), ask the user now: "DoD validado — quer que eu marque a PR como ready e habilite auto-merge, ou prefere olhar o código primeiro?" Only on an explicit yes, do all three together (mechanics in `open-pr` step 7): promote, arm auto-merge, sync the artifact.
```bash
gh pr ready <pr-number>
gh pr merge <pr-number> --auto --merge
```
If no PR exists yet, hand off to `/open-pr` — it opens the PR as Draft and asks this same question itself (step 7), syncing the artifact at that point too.

**Note:** the unideas board has `Backlog` / `Todo` / `In Progress` / `Done` / `Released` (no `In Review`) — the card stays in "In Progress" here, even with DoD green and the PR promoted. The sweep to "Done" (closing the issue, moving the card, syncing the parent epic) happens later, once the PR has actually merged into `dev`, on the next `/start-feature` run — that's a fact-check against reality (did it merge?), not a self-assessment, so it's kept separate from this skill. `Released` is a further, later step tied to an actual shipped version.

---

## Common mistakes

| Mistake | Fix |
|---|---|
| Validating DoD after the PR already merged | Validate before the PR is created (or before a Draft is promoted) — this is a pre-merge gate, not post-merge bookkeeping |
| Marking DoD done without checking commits | Confirm each item against the real diff (`git log dev..HEAD`) |
| Rewriting checklist wording without asking | Scope drift must be confirmed with the user before the issue body changes |
| Promoting a Draft to ready, or arming auto-merge, because "DoD passed" | Never on Claude's own initiative — always ask, every time, no exception |
| Closing a parent epic on `subIssuesSummary` alone | Its own body checklist is its DoD too — reconcile it (step 2) before closing |
| Treating "DoD green" as "move card to Done" | Card movement waits for the actual merge, checked by `/start-feature`'s next run — not by this skill |
| Syncing the Improvements artifact right after DoD passes, while the PR is still Draft | Wait for the user's explicit go-ahead to promote — sync happens together with `gh pr ready` (`open-pr` step 7), not before |
| Waiting for `/start-feature` to sync the Improvements artifact | Sync it at the ready-promotion moment (step 4 / `open-pr` step 7), not at the next `/start-feature` run |
