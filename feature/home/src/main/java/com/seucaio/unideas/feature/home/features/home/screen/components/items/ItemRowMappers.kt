package com.seucaio.unideas.feature.home.features.home.screen.components.items

import androidx.compose.runtime.Composable
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.lists.model.ListItemUi
import com.seucaio.unideas.feature.home.features.home.viewmodel.HomeMode

@Composable
internal fun Item.toListItemUi(
    checkContentDescription: String,
    homeMode: HomeMode = HomeMode.Normal,
): ListItemUi = ListItemUi(
    id = id,
    title = title,
    meta = if (isRecurring) recurrence.summaryLabel(dueDate) else null,
    showCheckbox = type == ItemType.TASK,
    checked = isCompleted,
    showRepeatIcon = dueDate != null && isRecurring,
    badgeLabel = dueBadgeLabel(this),
    badgeColor = dueBadgeColor(this),
    checkContentDescription = checkContentDescription,
    isSelected = when (homeMode) {
        is HomeMode.Selection -> id in homeMode.selectedItemIds
        HomeMode.Normal -> null
    },
    isPinned = isPinned,
    description = markdownDescriptionAnnotatedString(description),
)
