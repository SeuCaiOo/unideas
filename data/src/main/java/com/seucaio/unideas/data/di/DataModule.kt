package com.seucaio.unideas.data.di

import com.seucaio.unideas.data.local.database.UnideasDatabase
import com.seucaio.unideas.data.repository.ItemRepositoryImpl
import com.seucaio.unideas.domain.repository.ItemRepository
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
    singleOf(::ItemRepositoryImpl).bind<ItemRepository>()
}
