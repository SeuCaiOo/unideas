package com.seucaio.unideas.feature.tags.viewmodel

import com.seucaio.unideas.domain.model.Tag

/** Which entity dialog (if any) is showing on top of the tags list. */
sealed interface TagsDialogState {

    @Deprecated(
        "Only meaningful as \"no dialog open\" for the deprecated TagsScreen (V1). " +
            "TagsScreenV2 never opens a dialog for this case either way.",
    )
    data object None : TagsDialogState

    @Deprecated(
        "Only rendered as a dialog by the deprecated TagsScreen (V1) — TagsScreenV2's " +
            "Add row is always visible, never dialog-gated by this state.",
    )
    data object Add : TagsDialogState

    @Deprecated(
        "Only rendered as a dialog by the deprecated TagsScreen (V1) — TagsScreenV2 " +
            "reads this to pick which row renders inline-editable, it never opens a dialog for it.",
    )
    data class Rename(val tag: Tag) : TagsDialogState

    data class Delete(val tag: Tag) : TagsDialogState
}
