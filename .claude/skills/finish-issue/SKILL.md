---
name: finish-issue
description: Validates DoD against what was implemented and updates issue checkboxes — this is Claude's self-check, run before opening a PR or before asking the user whether an open Draft PR can be promoted. It never decides Draft-vs-ready itself; that's always the user's explicit call.
---

# Finish Issue — unideas Workflow

## When to run this

DoD validation is a **pre-merge gate**, not post-merge bookkeeping — it must happen before a PR becomes mergeable, not after. Two entry points, same skill either way:

1. **Implementation just finished, no PR yet** → run this first. If it passes, hand off to `/open-pr`, which asks the user Draft-vs-ready at creation time (step 6) — DoD status never decides that on its own.
2. **A PR is already open, Draft or ready, but not yet merged** (user chose Draft at creation time, or it's ready but hasn't been given an explicit merge instruction yet) → run this once you believe the work is complete.

**This skill validates whether the work is done. It never decides whether the PR gets promoted to ready, or merged/gets auto-merge armed — that is the user's call, always, asked explicitly, with zero exceptions, and Draft-vs-ready and merge-vs-not are two separate questions.** DoD passing is Claude's self-check that the checklist matches the diff; it is not the user having looked at the code. Confirmed the hard way: PR #38 (issue #24) got auto-merge armed the instant it opened, leaving no review window at all; later, #134 (2026-08-10) repeated the same failure in a different shape — "ready" got treated as authorization to merge, and an empty-diff PR (a separate bug, see `open-pr` step 2.5) merged into the epic branch before the user had a chance to open it. Draft-vs-ready and merge-vs-not-yet are both the user's decision alone, asked separately, never something DoD status can authorize.

---

## Step-by-step

### 1. Fetch issue (and PR, if one already exists)
```bash
gh issue view <issue-number> --json number,title,body,state
gh pr view <pr-number> --json number,title,url,headRefName,isDraft   # only if a PR already exists
```

### 2. Reconcile DoD against the real diff

Start from the local plan file (`.claude/plans/<type>-#<number>-*.md`) — its `## Checklist`/`## Verification` boxes should already be checked as items were completed during implementation (per `CLAUDE.md`'s Implementation workflow step 6), so it's the fast path to what's done instead of re-deriving everything from the diff. Treat it as a starting hypothesis, not ground truth: confirm each checked box still holds against `git log dev..HEAD` / `git diff dev..HEAD` before relying on it, since the plan file can drift or be stale. The plan's `## Verification` section deliberately omits the issue DoD's "PR aberto/mergeado" line (that's GitHub-only state — see step 3 below), so that one always needs a live check regardless of what the plan file says.

Compare the issue's Checklist/DoD section against that reconciled state. Every item lands in one of three buckets:

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

The DoD's "PR aberto, revisado e mergeado em `dev`" line stays unchecked here regardless of how green everything else is — it's only true once the PR has actually merged, which this step, by definition, runs before.

### 4. Report — and ask, don't act, on promotion

```
✅ DoD validado para a issue #N.
```

**Don't sync the Improvements artifact yet — that waits for the merge moment, not DoD passing, and not the ready-promotion moment either.** DoD green only means Claude's self-check passed; the user might still ask for changes before agreeing to ship. Marking the artifact "done" now, only to have the user request edits while the PR sits open, would leave it lying about the actual state.

If a PR already exists, ask the user now: "DoD validado — quer que eu marque/mergeie agora, ou prefere olhar o PR primeiro?" Only on an explicit yes to merge, do all of the following together (mechanics in `open-pr` step 7): promote to ready if still Draft, merge (auto or direct depending on whether the target branch has a required check), sync the artifact.
```bash
gh pr ready <pr-number>   # only if still Draft
gh pr merge <pr-number> --auto --merge
```
If no PR exists yet, hand off to `/open-pr` — it asks Draft-vs-ready itself at creation time (step 6); merging is always its own later, separate ask (step 7), never bundled into creation.

**Note:** the unideas board has `Backlog` / `Todo` / `In Progress` / `Done` / `Released` (no `In Review`) — the card stays in "In Progress" here, even with DoD green and the PR promoted. The sweep to "Done" (closing the issue, moving the card, syncing the parent epic) happens later, once the PR has actually merged into `dev`, on the next `/start-feature` run — that's a fact-check against reality (did it merge?), not a self-assessment, so it's kept separate from this skill. `Released` is a further, later step tied to an actual shipped version.

---

## Common mistakes

| Mistake | Fix |
|---|---|
| Validating DoD after the PR already merged | Validate before the PR is created (or before a Draft is promoted) — this is a pre-merge gate, not post-merge bookkeeping |
| Marking DoD done without checking commits | Confirm each item against the real diff (`git log dev..HEAD`) |
| Rewriting checklist wording without asking | Scope drift must be confirmed with the user before the issue body changes |
| Promoting a Draft to ready, or arming auto-merge/merging, because "DoD passed" | Never on Claude's own initiative — always ask, every time, no exception |
| Treating "ready" as authorization to merge | Ready only means non-draft/visible — merging is always its own separate, explicit ask (step 4 / `open-pr` step 7), even for a PR that's already ready |
| Closing a parent epic on `subIssuesSummary` alone | Its own body checklist is its DoD too — reconcile it (step 2) before closing |
| Treating "DoD green" as "move card to Done" | Card movement waits for the actual merge, checked by `/start-feature`'s next run — not by this skill |
| Syncing the Improvements artifact right after DoD passes, or right after the PR goes ready, but before it's merged | Wait for the user's explicit go-ahead to merge — sync happens together with the actual merge (`open-pr` step 7), not before |
| Waiting for `/start-feature` to sync the Improvements artifact | Sync it at the merge moment (step 4 / `open-pr` step 7), not at the next `/start-feature` run |
