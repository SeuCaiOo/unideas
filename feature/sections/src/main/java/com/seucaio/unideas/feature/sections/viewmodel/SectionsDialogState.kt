package com.seucaio.unideas.feature.sections.viewmodel

import com.seucaio.unideas.domain.model.Section

/** Which entity dialog (if any) is showing on top of the sections list. */
sealed interface SectionsDialogState {

    @Deprecated(
        "Only meaningful as \"no dialog open\" for the deprecated SectionsScreen (V1). " +
            "SectionsScreenV2 never opens a dialog for this case either way.",
    )
    data object None : SectionsDialogState

    @Deprecated(
        "Only rendered as a dialog by the deprecated SectionsScreen (V1) — SectionsScreenV2's " +
            "Add row is always visible, never dialog-gated by this state.",
    )
    data object Add : SectionsDialogState

    @Deprecated(
        "Only rendered as a dialog by the deprecated SectionsScreen (V1) — SectionsScreenV2 " +
            "reads this to pick which row renders inline-editable, it never opens a dialog for it.",
    )
    data class Rename(val section: Section) : SectionsDialogState

    data class Delete(val section: Section) : SectionsDialogState
}
