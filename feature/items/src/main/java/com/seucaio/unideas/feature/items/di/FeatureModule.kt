package com.seucaio.unideas.feature.items.di

import com.seucaio.unideas.feature.items.features.detail.viewmodel.ItemDetailViewModel
import com.seucaio.unideas.feature.items.features.form.viewmodel.ItemFormViewModel
import com.seucaio.unideas.feature.items.features.list.viewmodel.ItemsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val itemsModule = module {
    // itemId is a nav argument, not resolvable by Koin — passed via parametersOf at the call site.
    viewModel { params ->
        ItemFormViewModel(
            itemId = params.getOrNull(),
            getItem = get(),
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
            deleteItem = get(),
            completeItem = get(),
        )
    }
    viewModelOf(::ItemsListViewModel)
}
