package com.seucaio.unideas.feature.items.di

import com.seucaio.unideas.feature.items.ui.screens.additem.viewmodel.AddItemViewModel
import com.seucaio.unideas.feature.items.ui.screens.form.viewmodel.ItemFormViewModel
import com.seucaio.unideas.feature.items.ui.screens.list.viewmodel.ItemsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val itemsModule = module {
    // itemId/initialType are nav arguments, not resolvable by Koin — passed via parametersOf(itemId,
    // type) at the call site; ParametersHolder reads them back in that same order.
    viewModel { params ->
        ItemFormViewModel(
            itemId = params.getOrNull(),
            itemFormUseCase = get(),
            getSectionsAndTags = get(),
            initialType = params.get(),
        )
    }
    // initialType is a nav argument (the sheet's create-type, e.g. TASK vs NOTE) — passed via
    // parametersOf(initialType) at the call site.
    viewModel { params ->
        AddItemViewModel(
            createItem = get(),
            getSectionsAndTags = get(),
            initialType = params.get(),
        )
    }
    viewModelOf(::ItemsListViewModel)
}
