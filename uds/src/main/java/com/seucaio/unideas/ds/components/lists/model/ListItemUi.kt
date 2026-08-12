package com.seucaio.unideas.ds.components.lists.model

import androidx.compose.ui.graphics.Color

data class ListItemUi(
    val id: Long,
    val title: String,
    val meta: String?,
    val showCheckbox: Boolean,
    val checked: Boolean,
    val showRepeatIcon: Boolean,
    val badgeLabel: String?,
    val badgeColor: Color,
    val checkContentDescription: String,
    val isSelected: Boolean? = null,
)
