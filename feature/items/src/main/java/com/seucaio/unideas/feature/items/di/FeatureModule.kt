package com.seucaio.unideas.feature.items.di

import com.seucaio.unideas.feature.items.ui.screens.config.viewmodel.ItemConfigViewModel
import com.seucaio.unideas.feature.items.ui.screens.config.viewmodel.sectionstags.SectionsTagsViewModel
import com.seucaio.unideas.feature.items.ui.screens.detail.itemdetail.viewmodel.ItemDetailViewModel
import com.seucaio.unideas.feature.items.ui.screens.detail.itemoccurrence.viewmodel.ItemOccurrenceViewModel
import com.seucaio.unideas.feature.items.ui.screens.history.viewmodel.ItemHistoryViewModel
import com.seucaio.unideas.feature.items.ui.screens.list.viewmodel.ItemsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val itemsModule = module {
    // itemId/initialType are nav arguments, not resolvable by Koin — passed via parametersOf(itemId,
    // type) at the call site; ParametersHolder reads them back in that same order.
    viewModel { params ->
        ItemDetailViewModel(
            itemId = params.getOrNull(),
            itemFormUseCase = get(),
            sectionsAndTagsUseCase = get(),
            setItemArchivedUseCase = get(),
            savedStateHandle = get(),
            initialType = params.get(),
        )
    }
    viewModel { params ->
        ItemOccurrenceViewModel(
            itemId = params.getOrNull(),
            itemFormUseCase = get(),
            itemOccurrenceUseCase = get(),
        )
    }
    viewModel { params ->
        ItemHistoryViewModel(
            itemId = params.get(),
            itemOccurrenceUseCase = get(),
        )
    }
    viewModel { params ->
        ItemConfigViewModel(
            itemId = params.get(),
            itemFormUseCase = get(),
        )
    }
    viewModelOf(::ItemsListViewModel)
    viewModelOf(::SectionsTagsViewModel)
}
