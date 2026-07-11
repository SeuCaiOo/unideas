package com.seucaio.unideas.feature.sections.viewmodel

import com.seucaio.unideas.core.common.crud.EntityCrudViewModel
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.usecase.section.AddSectionUseCase
import com.seucaio.unideas.domain.usecase.section.DeleteSectionUseCase
import com.seucaio.unideas.domain.usecase.section.GetSectionsUseCase
import com.seucaio.unideas.domain.usecase.section.RenameSectionUseCase
import com.seucaio.unideas.feature.sections.R

class SectionsViewModel(
    getSections: GetSectionsUseCase,
    addSection: AddSectionUseCase,
    renameSection: RenameSectionUseCase,
    deleteSection: DeleteSectionUseCase,
) : EntityCrudViewModel<Section>(
    operations = SectionCrudOperations(getSections, addSection, renameSection, deleteSection),
    loadErrorRes = R.string.sections_load_error,
    nameRequiredRes = R.string.section_name_required,
    deleteBlockedRes = R.string.section_delete_blocked,
)
