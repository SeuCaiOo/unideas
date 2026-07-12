package com.seucaio.unideas.core.backup.di

import com.seucaio.unideas.core.backup.data.repository.BackupRepositoryImpl
import com.seucaio.unideas.core.backup.data.repository.GoogleAuthRepositoryImpl
import com.seucaio.unideas.core.backup.domain.repository.BackupRepository
import com.seucaio.unideas.core.backup.domain.repository.GoogleAuthRepository
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.BuildDriveServiceUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GetLastBackupInfoUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GetSignInIntentUseCase
import com.seucaio.unideas.core.backup.domain.usecase.ListBackupsUseCase
import com.seucaio.unideas.core.backup.domain.usecase.RestoreBackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.UploadBackupUseCase
import com.seucaio.unideas.core.backup.viewmodel.BackupViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Koin module for `:core:backup` — DI is local to the module, per project convention. */
val backupDataModule = module {
    single { GoogleAuthRepositoryImpl(androidApplication()) }.bind<GoogleAuthRepository>()
    single { BackupRepositoryImpl(database = get(), context = androidContext()) }.bind<BackupRepository>()
    factoryOf(::GetSignInIntentUseCase)
    factoryOf(::BuildDriveServiceUseCase)
    factoryOf(::UploadBackupUseCase)
    factoryOf(::ListBackupsUseCase)
    factoryOf(::RestoreBackupUseCase)
    factoryOf(::GetLastBackupInfoUseCase)
    factoryOf(::BackupUseCase)
    viewModelOf(::BackupViewModel)
}
