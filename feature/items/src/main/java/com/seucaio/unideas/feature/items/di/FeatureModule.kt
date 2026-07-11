package com.seucaio.unideas.feature.items.di

import com.seucaio.unideas.feature.items.viewmodel.ItemDetailViewModel
import com.seucaio.unideas.feature.items.viewmodel.ItemFormViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val itemsModule = module {
    // itemId is a nav argument, not resolvable by Koin — passed via parametersOf at the call site.
    viewModel { params ->
        ItemFormViewModel(
            itemId = params.getOrNull(),
            getItemDetail = get(),
            getSections = get(),
            getTags = get(),
            createItem = get(),
            editItem = get(),
        )
    }
    viewModel { params ->
        ItemDetailViewModel(
            itemId = params.get(),
            getItemDetail = get(),
            getSections = get(),
            deleteItem = get(),
            completeItem = get(),
        )
    }
}
