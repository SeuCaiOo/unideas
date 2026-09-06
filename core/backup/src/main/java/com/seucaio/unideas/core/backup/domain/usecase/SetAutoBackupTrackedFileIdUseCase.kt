package com.seucaio.unideas.core.backup.domain.usecase

import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository

class SetAutoBackupTrackedFileIdUseCase(private val repository: AutoBackupRepository) {

    suspend operator fun invoke(fileId: String?) = repository.setTrackedFileId(fileId)
}
