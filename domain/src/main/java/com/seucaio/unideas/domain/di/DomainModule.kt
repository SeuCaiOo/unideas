package com.seucaio.unideas.domain.di

import com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase
import com.seucaio.unideas.domain.usecase.item.CompleteItemUseCase
import com.seucaio.unideas.domain.usecase.item.CreateItemUseCase
import com.seucaio.unideas.domain.usecase.item.DeleteItemUseCase
import com.seucaio.unideas.domain.usecase.item.EditItemUseCase
import com.seucaio.unideas.domain.usecase.item.GetItemDetailUseCase
import com.seucaio.unideas.domain.usecase.item.GetItemUseCase
import com.seucaio.unideas.domain.usecase.item.GetItemsUseCase
import com.seucaio.unideas.domain.usecase.item.ItemUseCase
import com.seucaio.unideas.domain.usecase.section.AddSectionUseCase
import com.seucaio.unideas.domain.usecase.section.DeleteSectionUseCase
import com.seucaio.unideas.domain.usecase.section.GetSectionsUseCase
import com.seucaio.unideas.domain.usecase.section.RenameSectionUseCase
import com.seucaio.unideas.domain.usecase.section.SectionUseCase
import com.seucaio.unideas.domain.usecase.tag.AddTagUseCase
import com.seucaio.unideas.domain.usecase.tag.DeleteTagUseCase
import com.seucaio.unideas.domain.usecase.tag.GetTagsUseCase
import com.seucaio.unideas.domain.usecase.tag.RenameTagUseCase
import com.seucaio.unideas.domain.usecase.tag.TagUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin module for `:domain` use cases. Included by `appModule` in `:app`.
 * Grows one screen's worth of use cases at a time — the rest of the Item use
 * cases (GetPriorityItems) join once their own screens wire them (D2).
 */
val domainModule = module {
    factoryOf(::GetSectionsUseCase)
    factoryOf(::AddSectionUseCase)
    factoryOf(::RenameSectionUseCase)
    factoryOf(::DeleteSectionUseCase)
    factoryOf(::SectionUseCase)

    factoryOf(::GetTagsUseCase)
    factoryOf(::AddTagUseCase)
    factoryOf(::RenameTagUseCase)
    factoryOf(::DeleteTagUseCase)
    factoryOf(::TagUseCase)

    factoryOf(::GetSectionsAndTagsUseCase)

    factoryOf(::GetItemUseCase)
    factoryOf(::GetItemDetailUseCase)
    factoryOf(::GetItemsUseCase)
    factoryOf(::CreateItemUseCase)
    factoryOf(::EditItemUseCase)
    factoryOf(::DeleteItemUseCase)
    factoryOf(::CompleteItemUseCase)
    factoryOf(::ItemUseCase)
}
