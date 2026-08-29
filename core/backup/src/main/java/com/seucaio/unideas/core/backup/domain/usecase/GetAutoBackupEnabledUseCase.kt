package com.seucaio.unideas.core.backup.domain.usecase

import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository

class GetAutoBackupEnabledUseCase(private val repository: AutoBackupRepository) {

    suspend operator fun invoke(): Boolean = repository.isEnabled()
}
