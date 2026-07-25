package com.seucaio.unideas.ds.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

/**
 * Subtle background alpha for a pinned Section — shared between its header and its rows, so a
 * pinned group reads as one visual block, not just a highlighted title.
 */
private const val PINNED_BACKGROUND_ALPHA = 0.08f

/** [MaterialTheme.colorScheme.primary] tinted at [PINNED_BACKGROUND_ALPHA] when [isPinned], transparent otherwise. */
@Composable
fun pinnedBackgroundColor(isPinned: Boolean): Color =
    if (isPinned) MaterialTheme.colorScheme.primary.copy(alpha = PINNED_BACKGROUND_ALPHA) else Color.Transparent

/**
 * [base] with [pinnedBackgroundColor] blended on top when [isPinned] — a single opaque-ish color
 * to pass as a component's own `containerColor`, so the tint respects that component's rounded
 * clip instead of painting a flat rectangle behind it.
 */
@Composable
fun pinnedContainerColor(isPinned: Boolean, base: Color): Color =
    if (isPinned) pinnedBackgroundColor(isPinned).compositeOver(base) else base
