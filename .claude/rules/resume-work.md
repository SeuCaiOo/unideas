---
paths:
  - ".claude/plans/**"
---

# Resuming work ("continua", "onde paramos")

Opening a plan file mid-conversation means the user likely wants to pick up where things stood. Don't guess — walk this chain in order and stop at the first step that needs a decision, surfacing it as an explicit question:

1. **Branch** — `git branch --show-current`; identify which issue/epic it's for.
2. **Checklist** — read this plan's checklist state.
   - Nothing checked and no commits past the branch's base → confirm before starting the first item (e.g. "tem um plano pronto pra isso, posso começar o item 1?"). Don't silently start implementing just because a plan exists.
   - Partially done → the next unchecked item is the candidate next step — confirm it, don't assume it.
   - Fully checked → move to step 3.
3. **PR** — `gh pr list --head <branch> --state all` (plain `gh pr list` defaults to open-only and will falsely read as "no PR" for a closed/merged one — always pass `--state all`, or use `gh pr view` if unsure).
   - No PR (empty even with `--state all`) → ask if they want one opened (`open-pr`/`finish-issue`).
   - Open, unmerged → report its status, ask if they want it reviewed/merged.
   - Closed without merging → surface that explicitly and ask what to do (reopen, open a new one, abandon) — don't treat it as "no PR".
   - Merged → move to step 4.
4. **Epic** — does the issue belong to an epic with more sub-issues?
   - Pending sub-issues → ask if they want the next one pulled in (`start-feature`).
   - Epic fully done (or issue was standalone) → ask what's next (another backlog item, a release/version bump, etc.) — don't assume.

Each step must be verified against real state (`git status`, this file's checkboxes, `gh pr`/`gh issue` output), not inferred from conversation memory or a nearby step "seeming" resolved — the question asked at the end should reflect what's actually true right now, not a guess.
