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
