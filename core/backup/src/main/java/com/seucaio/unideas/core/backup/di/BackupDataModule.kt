package com.seucaio.unideas.core.backup.di

import com.seucaio.unideas.core.backup.data.local.datastore.AutoBackupPreferences
import com.seucaio.unideas.core.backup.data.repository.AutoBackupRepositoryImpl
import com.seucaio.unideas.core.backup.data.repository.BackupRepositoryImpl
import com.seucaio.unideas.core.backup.data.repository.GoogleAuthRepositoryImpl
import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository
import com.seucaio.unideas.core.backup.domain.repository.BackupRepository
import com.seucaio.unideas.core.backup.domain.repository.GoogleAuthRepository
import com.seucaio.unideas.core.backup.domain.usecase.AutoBackupSettingsUseCase
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.BuildDriveServiceUseCase
import com.seucaio.unideas.core.backup.domain.usecase.DeleteBackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GetAutoBackupEnabledUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GetLastBackupInfoUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GetSignInIntentUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GetSignedInAccountUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GoogleAuthUseCase
import com.seucaio.unideas.core.backup.domain.usecase.ListBackupsUseCase
import com.seucaio.unideas.core.backup.domain.usecase.PerformAutoBackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.RestoreBackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.SetAutoBackupEnabledUseCase
import com.seucaio.unideas.core.backup.domain.usecase.SignOutUseCase
import com.seucaio.unideas.core.backup.domain.usecase.UploadBackupUseCase
import com.seucaio.unideas.core.backup.viewmodel.BackupViewModel
import com.seucaio.unideas.core.backup.worker.AutoBackupTriggerImpl
import com.seucaio.unideas.core.backup.worker.AutoBackupWorker
import com.seucaio.unideas.domain.repository.AutoBackupTrigger
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Koin module for `:core:backup` — DI is local to the module, per project convention. */
val backupDataModule = module {
    single { GoogleAuthRepositoryImpl(androidApplication()) }.bind<GoogleAuthRepository>()
    single { BackupRepositoryImpl(database = get(), context = androidContext()) }.bind<BackupRepository>()
    single { AutoBackupPreferences(androidContext()) }
    single { AutoBackupRepositoryImpl(get()) }.bind<AutoBackupRepository>()
    single { AutoBackupTriggerImpl(androidContext()) }.bind<AutoBackupTrigger>()
    factoryOf(::GetSignInIntentUseCase)
    factoryOf(::GetSignedInAccountUseCase)
    factoryOf(::SignOutUseCase)
    factoryOf(::BuildDriveServiceUseCase)
    factoryOf(::UploadBackupUseCase)
    factoryOf(::ListBackupsUseCase)
    factoryOf(::RestoreBackupUseCase)
    factoryOf(::DeleteBackupUseCase)
    factoryOf(::GetLastBackupInfoUseCase)
    factoryOf(::GetAutoBackupEnabledUseCase)
    factoryOf(::SetAutoBackupEnabledUseCase)
    factoryOf(::AutoBackupSettingsUseCase)
    factoryOf(::PerformAutoBackupUseCase)
    factoryOf(::GoogleAuthUseCase)
    factoryOf(::BackupUseCase)
    viewModelOf(::BackupViewModel)
    workerOf(::AutoBackupWorker)
}
