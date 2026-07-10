package com.seucaio.unideas.domain.di

import com.seucaio.unideas.domain.usecase.section.AddSectionUseCase
import com.seucaio.unideas.domain.usecase.section.DeleteSectionUseCase
import com.seucaio.unideas.domain.usecase.section.GetSectionsUseCase
import com.seucaio.unideas.domain.usecase.section.RenameSectionUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin module for `:domain` use cases. Included by `appModule` in `:app`.
 * Grows one screen's worth of use cases at a time — Item/Tag use cases join
 * once their own screens wire them (D1, #44).
 */
val domainModule = module {
    factoryOf(::GetSectionsUseCase)
    factoryOf(::AddSectionUseCase)
    factoryOf(::RenameSectionUseCase)
    factoryOf(::DeleteSectionUseCase)
}
