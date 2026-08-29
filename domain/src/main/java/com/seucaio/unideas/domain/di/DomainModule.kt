package com.seucaio.unideas.domain.di

import com.seucaio.unideas.domain.usecase.SectionsAndTagsUseCase
import com.seucaio.unideas.domain.usecase.item.CompleteItemUseCase
import com.seucaio.unideas.domain.usecase.item.CreateItemUseCase
import com.seucaio.unideas.domain.usecase.item.DeleteItemUseCase
import com.seucaio.unideas.domain.usecase.item.EditItemUseCase
import com.seucaio.unideas.domain.usecase.item.ExtendItemDueDateUseCase
import com.seucaio.unideas.domain.usecase.item.GetArchivedItemsUseCase
import com.seucaio.unideas.domain.usecase.item.GetItemDetailUseCase
import com.seucaio.unideas.domain.usecase.item.GetItemUseCase
import com.seucaio.unideas.domain.usecase.item.GetItemsUseCase
import com.seucaio.unideas.domain.usecase.item.GetItemsWithDueDateUseCase
import com.seucaio.unideas.domain.usecase.item.GetPriorityItemsUseCase
import com.seucaio.unideas.domain.usecase.item.HasAnyItemUseCase
import com.seucaio.unideas.domain.usecase.item.HomeUseCase
import com.seucaio.unideas.domain.usecase.item.IgnoreOccurrenceUseCase
import com.seucaio.unideas.domain.usecase.item.ItemArchiveUseCase
import com.seucaio.unideas.domain.usecase.item.ItemCompletionHistoryUseCase
import com.seucaio.unideas.domain.usecase.item.ItemFormUseCase
import com.seucaio.unideas.domain.usecase.item.ItemOccurrenceUseCase
import com.seucaio.unideas.domain.usecase.item.ProcessMissedOccurrencesUseCase
import com.seucaio.unideas.domain.usecase.item.SetItemArchivedUseCase
import com.seucaio.unideas.domain.usecase.item.SetItemPinnedUseCase
import com.seucaio.unideas.domain.usecase.item.SetRemindersMutedUseCase
import com.seucaio.unideas.domain.usecase.onboarding.GetOnboardingSeenUseCase
import com.seucaio.unideas.domain.usecase.onboarding.SetOnboardingSeenUseCase
import com.seucaio.unideas.domain.usecase.section.AddSectionUseCase
import com.seucaio.unideas.domain.usecase.section.DeleteSectionUseCase
import com.seucaio.unideas.domain.usecase.section.GetSectionsUseCase
import com.seucaio.unideas.domain.usecase.section.RenameSectionUseCase
import com.seucaio.unideas.domain.usecase.section.SectionUseCase
import com.seucaio.unideas.domain.usecase.section.SetSectionPinnedUseCase
import com.seucaio.unideas.domain.usecase.settings.ClearDatabaseUseCase
import com.seucaio.unideas.domain.usecase.settings.SeedDatabaseUseCase
import com.seucaio.unideas.domain.usecase.tag.AddTagUseCase
import com.seucaio.unideas.domain.usecase.tag.DeleteTagUseCase
import com.seucaio.unideas.domain.usecase.tag.GetTagsUseCase
import com.seucaio.unideas.domain.usecase.tag.RenameTagUseCase
import com.seucaio.unideas.domain.usecase.tag.TagUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    //region Sections
    factoryOf(::GetSectionsUseCase)
    factoryOf(::AddSectionUseCase)
    factoryOf(::RenameSectionUseCase)
    factoryOf(::DeleteSectionUseCase)
    factoryOf(::SetSectionPinnedUseCase)
    factoryOf(::SectionUseCase)
    //endregion

    //region Tags
    factoryOf(::GetTagsUseCase)
    factoryOf(::AddTagUseCase)
    factoryOf(::RenameTagUseCase)
    factoryOf(::DeleteTagUseCase)
    factoryOf(::TagUseCase)
    //endregion

    factoryOf(::SectionsAndTagsUseCase)

    //region Settings
    factoryOf(::SeedDatabaseUseCase)
    factoryOf(::ClearDatabaseUseCase)
    //endregion

    //region Items
    factoryOf(::GetItemUseCase)
    factoryOf(::GetItemDetailUseCase)
    factoryOf(::GetItemsUseCase)
    factoryOf(::CreateItemUseCase)
    factoryOf(::EditItemUseCase)
    factoryOf(::DeleteItemUseCase)
    factoryOf(::CompleteItemUseCase)
    factoryOf(::GetPriorityItemsUseCase)
    factoryOf(::SetItemPinnedUseCase)
    factoryOf(::SetItemArchivedUseCase)
    factoryOf(::GetArchivedItemsUseCase)
    factoryOf(::GetItemsWithDueDateUseCase)
    factoryOf(::HasAnyItemUseCase)
    factoryOf(::ItemFormUseCase)
    factoryOf(::ItemOccurrenceUseCase)
    factoryOf(::HomeUseCase)
    factoryOf(::ItemArchiveUseCase)
    factoryOf(::ItemCompletionHistoryUseCase)
    factoryOf(::ProcessMissedOccurrencesUseCase)
    factoryOf(::IgnoreOccurrenceUseCase)
    factoryOf(::ExtendItemDueDateUseCase)
    factoryOf(::SetRemindersMutedUseCase)
    //endregion

    //region Onboarding
    factoryOf(::GetOnboardingSeenUseCase)
    factoryOf(::SetOnboardingSeenUseCase)
    //endregion
}
