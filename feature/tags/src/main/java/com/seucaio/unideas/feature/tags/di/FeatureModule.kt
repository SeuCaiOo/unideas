package com.seucaio.unideas.feature.tags.di

import com.seucaio.unideas.feature.tags.viewmodel.TagsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val tagsModule = module {
    viewModelOf(::TagsViewModel)
}
