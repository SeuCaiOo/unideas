package com.seucaio.unideas.core.common.util

object Constants {

    /** Days ahead of the due date at which an item becomes "due soon" (urgency threshold). */
    const val DUE_SOON_DAYS = 3

    /** Maximum number of items shown in the Home priority panel. */
    const val PRIORITY_PANEL_LIMIT = 5

    /** NavBackStackEntry.savedStateHandle key: did the screen the user just left persist a change? */
    const val ITEM_SAVED_RESULT_KEY = "itemSaved"
}
