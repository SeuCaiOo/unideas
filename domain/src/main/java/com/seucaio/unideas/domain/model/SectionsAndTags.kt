package com.seucaio.unideas.domain.model

/**
 * The full set of sections and tags available for selection — a single snapshot for screens
 * that let the user pick from both at once (item form, Home's filters). Neither list can
 * change from within those screens (sections/tags only get created/edited in Settings), so
 * there is nothing to react to mid-session.
 */
data class SectionsAndTags(
    val sections: List<Section>,
    val tags: List<Tag>,
)
