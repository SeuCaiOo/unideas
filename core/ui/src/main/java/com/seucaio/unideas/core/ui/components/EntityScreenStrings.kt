package com.seucaio.unideas.core.ui.components

import androidx.annotation.StringRes

/**
 * Bundles the `@StringRes` ids an [EntityManagementScreen] needs — one object instead of ~10
 * loose Int parameters, since these strings live per-feature and can't be shared.
 */
data class EntityScreenStrings(
    @StringRes val title: Int,
    @StringRes val addLabel: Int,
    @StringRes val addFieldLabel: Int,
    @StringRes val renameLabel: Int,
    @StringRes val renameFieldLabel: Int,
    @StringRes val emptyMessage: Int,
    @StringRes val optionsDescription: Int,
    @StringRes val renameAction: Int,
    @StringRes val deleteAction: Int,
    @StringRes val deleteConfirmTitle: Int,
    @StringRes val deleteConfirmMessage: Int,
)
