package com.seucaio.unideas.ds.components.lists

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.ds.theme.pinnedBackgroundColor
import com.seucaio.unideas.ds.theme.pinnedContainerColor

/**
 * A clickable, collapsible variant of [GroupHeader] — title (+ item count) with a chevron that
 * rotates to reflect [expanded]. Used to group a lazy list's rows by section, expand/collapse per
 * group. [onTogglePin], when non-null, renders a pin toggle (filled when [isPinned]) — pinned
 * groups sort first in the caller's list. [indentStart], on top of the base start padding, reads
 * this group as nested under a meta-group divider above it (e.g. an emphasized [GroupHeader]
 * "Pinned") instead of a sibling at the same level. [isSelected], when non-null, renders a
 * select-all-in-group toggle (filled check when every item in the group is selected, outline
 * otherwise) — same on/off semantics as a single row's selection indicator, scoped to the group.
 */
@Composable
fun CollapsibleGroupHeader(
    title: String,
    itemCount: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isPinned: Boolean = false,
    onTogglePin: (() -> Unit)? = null,
    indentStart: Dp = 0.dp,
    isSelected: Boolean? = null,
    onToggleSelection: (() -> Unit)? = null,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevronRotation"
    )
    val labelColor =
        if (isPinned) MaterialTheme.colorScheme.primary else LocalUdsExtendedColors.current.textTertiary

    Row(
        modifier
            .fillMaxWidth()
            .background(pinnedBackgroundColor(isPinned))
            .clickable(onClick = onToggle)
            .padding(start = 20.dp + indentStart, end = 8.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            "${title.uppercase()} ($itemCount)",
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
            modifier = Modifier.weight(1f),
        )
        if (onTogglePin != null) {
            IconButton(onClick = onTogglePin, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = null,
                    tint = labelColor,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        if (isSelected != null) {
            IconButton(onClick = { onToggleSelection?.invoke() }, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else labelColor,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Icon(
            Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = LocalUdsExtendedColors.current.textTertiary,
            modifier = Modifier
                .size(18.dp)
                .rotate(rotation),
        )
    }
}

private enum class CollapsibleGroupHeaderPreviewScenario {
    Expanded, Collapsed, Pinned, SelectedInSelectionMode, UnselectedInSelectionMode, PinnedAndSelected, PinnedEndToEnd
}

private class CollapsibleGroupHeaderPreviewProvider :
    PreviewParameterProvider<CollapsibleGroupHeaderPreviewScenario> {
    override val values = CollapsibleGroupHeaderPreviewScenario.entries.asSequence()
}

@Composable
private fun WorkGroupHeader(
    title: String = "Work",
    itemCount: Int = 4,
    expanded: Boolean = true,
    isPinned: Boolean = false,
    onTogglePin: (() -> Unit)? = null,
    isSelected: Boolean? = null,
    onToggleSelection: (() -> Unit)? = null,
) {
    CollapsibleGroupHeader(
        title = title,
        itemCount = itemCount,
        expanded = expanded,
        onToggle = {},
        isPinned = isPinned,
        onTogglePin = onTogglePin,
        isSelected = isSelected,
        onToggleSelection = onToggleSelection,
    )
}

/**
 * [PinnedEndToEnd][CollapsibleGroupHeaderPreviewScenario.PinnedEndToEnd] shows the full shape of a
 * pinned group as `:feature:home` actually renders it — the emphasized "Pinned" [GroupHeader]
 * divider, an indented pinned [CollapsibleGroupHeader], and a pinned [ListItemRow] below it, all
 * in one glance instead of three separate previews the reader has to mentally stack.
 */
@PreviewLightDark
@Composable
private fun CollapsibleGroupHeaderPreview(
    @PreviewParameter(CollapsibleGroupHeaderPreviewProvider::class) scenario: CollapsibleGroupHeaderPreviewScenario,
) {
    UdsTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.background)) {
            when (scenario) {
                CollapsibleGroupHeaderPreviewScenario.Expanded -> WorkGroupHeader()
                CollapsibleGroupHeaderPreviewScenario.Collapsed ->
                    WorkGroupHeader(title = "Personal", itemCount = 12, expanded = false)
                CollapsibleGroupHeaderPreviewScenario.Pinned ->
                    WorkGroupHeader(isPinned = true, onTogglePin = {})
                CollapsibleGroupHeaderPreviewScenario.SelectedInSelectionMode ->
                    WorkGroupHeader(isSelected = true, onToggleSelection = {})
                CollapsibleGroupHeaderPreviewScenario.UnselectedInSelectionMode ->
                    WorkGroupHeader(isSelected = false, onToggleSelection = {})
                CollapsibleGroupHeaderPreviewScenario.PinnedAndSelected -> WorkGroupHeader(
                    isPinned = true,
                    onTogglePin = {},
                    isSelected = true,
                    onToggleSelection = {},
                )
                CollapsibleGroupHeaderPreviewScenario.PinnedEndToEnd -> {
                    GroupHeader("Pinned", emphasized = true)
                    CollapsibleGroupHeader(
                        title = "Work",
                        itemCount = 2,
                        expanded = true,
                        onToggle = {},
                        isPinned = true,
                        onTogglePin = {},
                        indentStart = 12.dp,
                    )
                    ListItemRow(
                        ui = ListItemUi(
                            id = 1L,
                            title = "Pay electricity bill",
                            meta = "Home",
                            showCheckbox = true,
                            checked = false,
                            showRepeatIcon = true,
                            badgeLabel = "6 days overdue",
                            badgeColor = MaterialTheme.colorScheme.error,
                            checkContentDescription = "Confirm",
                        ),
                        onClick = {},
                        onToggleCheck = {},
                        containerColor = pinnedContainerColor(
                            isPinned = true,
                            base = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}
