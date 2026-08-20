package com.seucaio.unideas.feature.items.ui.screens.config.sectionstags.viewmodel

import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.Tag

data class SectionsTagsUiState(
    val sections: List<Section> = emptyList(),
    val tags: List<Tag> = emptyList(),
)
