package com.seucaio.unideas.feature.home.features.home.screen.components

import androidx.compose.runtime.Composable
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.lists.ListItemUi

@Composable
internal fun Item.toListItemUi(
    checkContentDescription: String,
    selectedItemIds: Set<Long> = emptySet()
): ListItemUi =
    ListItemUi(
        id = id,
        title = title,
        meta = if (isRecurring) recurrence.summaryLabel(dueDate) else null,
        showCheckbox = type == ItemType.TASK,
        checked = isCompleted,
        showRepeatIcon = dueDate != null && isRecurring,
        badgeLabel = dueBadgeLabel(this),
        badgeColor = dueBadgeColor(this),
        checkContentDescription = checkContentDescription,
        isSelected = if (selectedItemIds.isEmpty()) null else id in selectedItemIds,
    )
