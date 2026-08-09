---
paths:
  - "**/*.kt"
---
# Comments

Default to no comments. Only add one when the WHY is genuinely non-obvious (a hidden constraint, a subtle invariant, a workaround for a specific bug) — never to restate what a well-named identifier already says.

Never write a comment that:
- Narrates a change and when it happened ("changed at 1pm", "now updated because...") — that belongs in the commit message, not the source.
- References another class/function/package by name — when that code gets renamed, the comment goes stale and forces an edit nobody asked for.
- Explains WHAT the code does instead of WHY it exists.

`//region` / `//endregion` (Kotlin/Android Studio grouping tags) are fine for organizing long files (see `SectionsViewModel.kt`, `TagsViewModel.kt`, `HomeViewModel.kt`) — this rule is about prose comments/KDoc, not code-folding tags.

**Writing a new public function/composable is not, by itself, a reason to add a KDoc.** The default habit — every new component gets a one-line `/** ... */` summarizing what it is — is exactly the "explains WHAT instead of WHY" violation above, just wearing a doc-comment costume instead of an inline one. A convention that already lives in a `.claude/rules/*.md` file (e.g. "sheets split into an outer function + a `*Content` inner one, preview calls the inner one") does not get restated at each individual call site that follows it — the rule file is the one place that fact lives; repeating it in every new file is the same staleness risk as narrating a change inline, multiplied by every file that copies it. Confirmed the hard way (#130, 2026-08-09): after documenting the sheet-preview pattern in `mvi.md`, every new sheet file (`RecurrenceBottomSheet`, `EveryNDaysBottomSheet`, `WeekdayBottomSheet`, `DayOfMonthBottomSheet`, `SelectionBottomSheet`, `GridSelectionBottomSheet`) still got its own copy of a comment re-explaining the same limitation — deleted from all of them.
