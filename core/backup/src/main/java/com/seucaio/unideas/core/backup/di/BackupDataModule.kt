package com.seucaio.unideas.core.backup.di

import com.seucaio.unideas.core.backup.data.repository.GoogleAuthRepositoryImpl
import com.seucaio.unideas.core.backup.domain.repository.GoogleAuthRepository
import com.seucaio.unideas.core.backup.domain.usecase.BuildDriveServiceUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GetSignInIntentUseCase
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for `:core:backup`. Not yet included by `appModule` in `:app` —
 * no screen consumes this flow until E1.2 (#30) / E2 (#16).
 */
val backupDataModule = module {
    single { GoogleAuthRepositoryImpl(androidApplication()) }.bind<GoogleAuthRepository>()
    factoryOf(::GetSignInIntentUseCase)
    factoryOf(::BuildDriveServiceUseCase)
}
