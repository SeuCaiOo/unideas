package com.seucaio.unideas.feature.items.di

import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailViewModel
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
            getSectionsAndTags = get(),
            savedStateHandle = get(),
            initialType = params.get(),
        )
    }
    viewModelOf(::ItemsListViewModel)
}
