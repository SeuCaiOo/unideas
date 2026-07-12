package com.seucaio.unideas.di

import com.seucaio.unideas.core.backup.di.backupDataModule
import com.seucaio.unideas.data.di.dataModule
import com.seucaio.unideas.domain.di.domainModule
import com.seucaio.unideas.feature.home.di.homeModule
import com.seucaio.unideas.feature.items.di.itemsModule
import com.seucaio.unideas.feature.sections.di.sectionsModule
import com.seucaio.unideas.feature.settings.di.settingsModule
import com.seucaio.unideas.feature.tags.di.tagsModule
import org.koin.dsl.module

val appModule = module {
    includes(
        dataModule,
        domainModule,
        backupDataModule,
        sectionsModule,
        tagsModule,
        settingsModule,
        itemsModule,
        homeModule,
    )
}
