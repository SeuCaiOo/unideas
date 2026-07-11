package com.seucaio.unideas.feature.tags.viewmodel

import com.seucaio.unideas.core.common.crud.EntityCrudViewModel
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.domain.usecase.tag.AddTagUseCase
import com.seucaio.unideas.domain.usecase.tag.DeleteTagUseCase
import com.seucaio.unideas.domain.usecase.tag.GetTagsUseCase
import com.seucaio.unideas.domain.usecase.tag.RenameTagUseCase
import com.seucaio.unideas.feature.tags.R

class TagsViewModel(
    getTags: GetTagsUseCase,
    addTag: AddTagUseCase,
    renameTag: RenameTagUseCase,
    deleteTag: DeleteTagUseCase,
) : EntityCrudViewModel<Tag>(
    operations = TagCrudOperations(getTags, addTag, renameTag, deleteTag),
    loadErrorRes = R.string.tags_load_error,
    nameRequiredRes = R.string.tag_name_required,
    deleteBlockedRes = R.string.tag_delete_blocked,
)
