package com.seucaio.unideas.feature.items.di

import com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailViewModel
import com.seucaio.unideas.feature.items.ui.screens.form.viewmodel.ItemFormViewModel
import com.seucaio.unideas.feature.items.ui.screens.list.viewmodel.ItemsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val itemsModule = module {
    // itemId/initialType are nav arguments, not resolvable by Koin — passed via parametersOf(itemId,
    // type) at the call site; ParametersHolder reads them back in that same order.
    viewModel { params ->
        _root_ide_package_.com.seucaio.unideas.feature.items.ui.screens.form.viewmodel.ItemFormViewModel(
            itemId = params.getOrNull(),
            itemFormUseCase = get(),
            getSectionsAndTags = get(),
            initialType = params.get(),
        )
    }
    viewModel { params ->
        _root_ide_package_.com.seucaio.unideas.feature.items.ui.screens.detail.viewmodel.ItemDetailViewModel(
            createItem = get(),
            getSectionsAndTags = get(),
            initialType = params.get(),
        )
    }
    viewModelOf(::ItemsListViewModel)
}
