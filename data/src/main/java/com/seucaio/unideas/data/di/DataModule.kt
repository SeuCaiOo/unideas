package com.seucaio.unideas.data.di

import com.seucaio.unideas.data.local.database.UnideasDatabase
import com.seucaio.unideas.data.repository.ItemRepositoryImpl
import com.seucaio.unideas.data.repository.SectionRepositoryImpl
import com.seucaio.unideas.data.repository.TagRepositoryImpl
import com.seucaio.unideas.domain.repository.ItemRepository
import com.seucaio.unideas.domain.repository.SectionRepository
import com.seucaio.unideas.domain.repository.TagRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for the `:data` layer — database, DAOs and repository
 * implementations. Included by `appModule` in `:app`.
 */
val dataModule = module {
    single { UnideasDatabase.getInstance(androidApplication()) }
    single { get<UnideasDatabase>().itemDao() }
    single { get<UnideasDatabase>().sectionDao() }
    single { get<UnideasDatabase>().tagDao() }
    singleOf(::ItemRepositoryImpl).bind<ItemRepository>()
    singleOf(::SectionRepositoryImpl).bind<SectionRepository>()
    singleOf(::TagRepositoryImpl).bind<TagRepository>()
}
