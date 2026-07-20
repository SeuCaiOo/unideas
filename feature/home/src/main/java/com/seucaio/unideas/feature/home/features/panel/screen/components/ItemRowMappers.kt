package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.runtime.Composable
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.ds.components.lists.ListItemUi

/**
 * V2 (#84): maps [Item] to `:uds`'s domain-agnostic [ListItemUi], so screens can call `:uds`'s
 * `ListItemRow` directly instead of going through a feature-local wrapper composable. Shared
 * between [com.seucaio.unideas.feature.home.features.panel.screen.HomeScreenV2] and
 * `com.seucaio.unideas.feature.home.features.browse.screen.BrowseScreen`.
 */
@Composable
internal fun Item.toListItemUi(checkContentDescription: String): ListItemUi = ListItemUi(
    id = id,
    title = title,
    meta = null,
    showCheckbox = type == ItemType.TASK,
    checked = isCompleted,
    showRepeatIcon = dueDate != null && isRecurring,
    badgeLabel = dueBadgeLabel(this),
    badgeColor = dueBadgeColor(this),
    checkContentDescription = checkContentDescription,
)
