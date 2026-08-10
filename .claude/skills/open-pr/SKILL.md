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
- Assignee: whoever opens the PR (`gh pr create --assignee "@me"`) — don't hardcode a username, since another dev may work on this project in the future
- Diff: compare commits only against the **target branch** (`git log dev..HEAD`), never against `main`

## Step-by-step

### 1. Branch

**If the work is tied to a numbered GitHub issue, this step already happened in `/start-feature` (which links the branch to the issue via `createLinkedBranch` at creation time) — don't recreate it here.** Only use the plain flow below for work with no issue number (ad-hoc chore/fix, no `/start-feature` involved):

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

### 2.5. Push and verify the diff — before `gh pr create`, every time

**`gh pr create` never pushes commits itself** — it opens a PR from whatever the remote branch already has. If local commits landed after the branch's last push (e.g. the branch was created early via `createLinkedBranch`, then many more commits happened locally during implementation), the PR can silently open empty or stale.

```bash
git push origin <branch>
git log <base-branch>..HEAD --oneline
git diff <base-branch>..HEAD --stat
```

Confirm the log/diff actually shows the commits and files expected before moving on — every time, not just when something feels off. Confirmed the hard way (#134, 2026-08-10): 12 local commits were never pushed before `gh pr create` ran, so the PR opened comparing the target branch against a 12-commits-behind remote ref — a near-empty diff (1 file, +14/-2) instead of the real change (34 files, +431/-760). Combined with auto-merge being armed in the same action (see step 6), the empty-diff PR merged undetected within seconds.

### 3. Verify test coverage
```bash
./gradlew clean
./gradlew koverVerify
```
**Always `clean` first** — stale build/configuration cache in this multi-module setup can report a coverage number that doesn't match reality (confirmed: a "failing" 31% turned out to be cache staleness, real number was well above minimum post-clean). The project is small; the extra seconds are cheap insurance against chasing a phantom failure.

**Must pass regardless of whether the PR opens Draft or ready** (Draft-vs-ready is asked explicitly at creation time — see step 6, not decided by this check). If it fails after a clean run, add missing tests first. `dev_checks.yml` (CI) enforces the same check on the PR.

### 3.5. Validate DoD

Run the `finish-issue` skill now, **before** creating the PR, if the issue is tied to a numbered GitHub issue. It reconciles the issue's DoD/Checklist against the real diff and updates the checkboxes. This is a pre-merge gate, not post-merge bookkeeping — don't defer it to after the PR exists.

**DoD status does not decide Draft vs. ready — it never has that authority.** Whichever way DoD comes out, step 6 asks the user which one they want. DoD passing only means the work itself is done; it says nothing about whether the user has looked at it yet, or whether they're ready to ship it right now.

No linked issue (ad-hoc chore/fix) → skip this step, there's no DoD to validate.

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

### 6. Ask Draft vs. ready — before creating the PR, every time

**DoD status never decides this — it's the user's call, both directions, asked explicitly every single time.** Right before `gh pr create`, ask: "PR pronta pra abrir — quer Draft ou já ready?" This isn't optional even when DoD is green and the work looks clean.

**"Ready" means non-draft and visible for review. It does NOT mean merge, and does NOT mean arm auto-merge** — those are a separate action, asked separately, only after the user has actually had a chance to open the PR on GitHub (see step 7). Never call `gh pr merge` (auto or direct) in the same action as `gh pr create`/`gh pr ready`.

Three things went wrong in the past from getting this ask wrong: PR #38 (issue #24) got auto-merge armed the instant it was created, leaving no review window at all ("o que define se ele tá draft ou ready? Eu decide"); later, always defaulting to Draft caused the opposite friction — the user had to separately ask for ready on every single PR that they actually wanted shipped immediately; then, on #134 (2026-08-10), asking "Draft ou já ready com auto-merge?" and bundling the merge into the same action as creation caused an empty-diff PR (see step 2.5) to merge unattended within seconds of opening — the user never got a chance to look at it first. In their own words: "Uma coisa é um PR ready pronto para eu poder olhar. Outra coisa é um PR em ready que você já mergeou e eu nem consegui ver o PR." Asking Draft-vs-ready at creation time, and asking to merge as a fully separate later step, fixes all three: no silent fast-path to an unattended merge, no busywork when the user already knows they want it visible, and always a real review window before anything merges.

**If Draft:**
```bash
gh pr create \
  --base dev \
  --head <branch> \
  --title "<EN title>" \
  --body "$(cat .github/PULL_REQUEST_TEMPLATE.md)" \
  --assignee "@me" \
  --draft
```

**If ready:**
```bash
gh pr create \
  --base dev \
  --head <branch> \
  --title "<EN title>" \
  --body "$(cat .github/PULL_REQUEST_TEMPLATE.md)" \
  --assignee "@me"
```

Either way, apply the label and report the PR URL, then **stop** — do not touch `gh pr merge`/`gh pr ready` (for auto-merge) until step 7's separate, explicit ask:
```bash
gh pr edit <number> --add-label "<label>"
```

Always include `Closes #<issue>` in the PR body at creation time (in the `gh pr create --body` call). Two separate things happen from this text, and it's worth not conflating them:

- **`closingIssuesReferences` / auto-close on merge**: stays empty and merging won't auto-close the issue, because `dev` isn't the repo's default branch (`willCloseTarget: false`). This is expected — final closing happens at the `dev`→`main` release PR, or manually.
- **The "Development" sidebar link on the issue** (a `ConnectedEvent` in the issue's timeline): this **does** appear from the same `Closes #N` text, confirmed live on issue #23 / PR #37 — it showed up within a few minutes of the PR being opened/pushed. No extra step needed. There is no public API to manually link an already-open PR to an issue (checked the GraphQL schema — only `createLinkedBranch`/`deleteLinkedBranch` exist, nothing PR-equivalent); the web UI's "Link a pull request" search box has no exposed mutation, so don't try to build a workaround for it.

If the sidebar link seems missing right after opening the PR, wait a few minutes before assuming it failed — it's a webhook/indexing delay, not a broken mechanism. The branch itself is separately linked at creation time via `createLinkedBranch` in `/start-feature` step 3.

### 7. Merging — **always its own separate, explicit ask, no exception, regardless of Draft or ready**

Applies whether the PR opened Draft or ready in step 6 — **"ready" in step 6 only got the PR out of Draft, it never authorized a merge.** Arming auto-merge (or merging directly), and syncing the Improvements artifact, all happen **together, at the same moment, only once the user explicitly says to merge** — not at DoD-green time, not just because the PR has been open/ready for a while, and not bundled into the same action that created or promoted the PR.

If the PR is still Draft, promote it to ready first (`gh pr ready <number>`) as part of this same step, once the user's merge instruction arrives — don't promote separately in advance of that instruction.

**Ask, every single time, regardless of how clean the work looks:** "DoD validado — quer que eu marque/mergeie agora, ou prefere olhar o PR primeiro?" The user may come back with more changes first ("melhora isso, ajusta aquilo") — those land as ordinary follow-up commits on the same branch, DoD gets re-validated if needed, and the same question gets asked again later. Only on an explicit yes to *that* question do the following happen, in this order — check first whether the target branch actually has a required CI check configured (`dev` does via `dev_checks.yml`; a long-lived epic branch may not, per its own protection rules) to decide between `--auto` and a direct merge:

```bash
gh pr ready <number>   # only if still Draft
gh pr merge <number> --auto --merge   # if the target branch has a required CI check
# or, if the target branch has no required check configured (verify: gh pr view <number> --json statusCheckRollup):
gh pr merge <number> --merge
```

Then sync the **"unideas — Improvements"** artifact (URL in `.claude/skills/add-improvement/SKILL.md`) for this issue, right now, in the same pass — don't wait for the PR to actually merge or for the user to pull the next issue:
1. `WebFetch` the artifact URL for its current markdown.
2. Find the entry whose heading contains `(#<issue-number>)`. Check every `- [ ]` in its checklist to `- [x]`.
3. Add/update its status tag: `· ✅ **Merged** (PR #<number> → dev, implementado via <how>)` — matching the existing convention, right after its `pré-req` line.
4. If the issue has a parent epic: update the parent's status tag too, and move it (and this issue) into **"## Finalizadas (Done)"** if this was the epic's last remaining sub-issue; otherwise keep the parent listed under "Em andamento" with the updated sub-issue count.
5. Write the full updated markdown to a local scratchpad file and republish via `Artifact` with the same `url` — never a new `file_path`-only publish.

**Why the artifact sync waits for this exact moment, not DoD-green time (or PR-ready time):** DoD green only means Claude's self-check passed — the user might still ask for changes while the PR sits open. If the artifact were marked done at DoD-green or ready time and the user then requested edits before agreeing to merge, the artifact would read "done" while the work was still actually in flux. The moment the user says "pode mergear" is the only point that's actually final. This is *also* why the sync shouldn't wait for the next `/start-feature` run either (the old timing) — that could leave it stale for however long the user takes to start something new, especially since auto-merge now runs unattended once armed. `start-feature` step 0 keeps a fallback pass for anything that slips through (skip any issue whose artifact entry already shows `✅ Merged` with the right PR number).

`dev` has branch protection requiring the `Quality Gate` check (from `dev_checks.yml`) to pass, and the repo has `allow_auto_merge` enabled — this only works because the repo is public (branch protection on private repos needs a paid GitHub plan). `main` is **deliberately not** auto-merged: it has no branch protection and merges there (the periodic `dev` → `main` release PR) are manual, matching a stricter review since `main_build.yml` runs the full signed release build. Don't run `gh pr merge --auto` on a PR targeting `main`.

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
| Running `gh pr merge --auto` on a `main`-targeting PR | Auto-merge is only for `dev`; `main` merges are manual and reviewed |
| Validating DoD after the PR is already open/mergeable | Run `finish-issue` (step 3.5) before opening the PR — DoD is a pre-merge gate, not something to check after the fact |
| Defaulting every PR to Draft without asking, or defaulting every PR to ready without asking | Neither is right — ask Draft-vs-ready explicitly at creation time (step 6), every time, regardless of DoD |
| Running `gh pr create` without pushing first, then trusting the resulting diff | Always `git push` + verify `git log`/`git diff --stat` against the base branch first (step 2.5) — `gh pr create` never pushes commits itself, and a stale remote branch produces a silently wrong/empty diff |
| Treating "ready" (step 6) as authorization to merge or arm auto-merge | "Ready" only means non-draft/visible — merging (step 7) is always its own separate, explicit ask, no matter how the Draft-vs-ready question was answered |
| Bundling `gh pr merge`/auto-merge into the same action as `gh pr create`/`gh pr ready` | Never — always wait for a distinct merge instruction after the user has had a chance to actually open the PR (step 7) |
| Running `gh pr ready`/`gh pr merge --auto` on an already-open Draft without asking, for any reason including "DoD passed" | Always ask first (step 7) — this is the one rule with zero exceptions in this whole skill |
| Syncing the Improvements artifact as soon as DoD passes, while the PR is still Draft (and the user hasn't said to promote it) | Wait for the user's explicit go-ahead to promote (step 7) — DoD green doesn't mean they won't ask for changes first |
| Waiting for the next `/start-feature` to sync the Improvements artifact | Sync it right when the user says "pode ficar ready" (step 7), not before and not at the next `/start-feature` |