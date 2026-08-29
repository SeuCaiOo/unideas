package com.seucaio.unideas.core.backup.domain.usecase

import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository

class SetAutoBackupEnabledUseCase(private val repository: AutoBackupRepository) {

    suspend operator fun invoke(enabled: Boolean) = repository.setEnabled(enabled)
}
