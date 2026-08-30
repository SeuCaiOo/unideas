package com.seucaio.unideas.core.backup.domain.usecase

import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository

class GetAutoBackupTrackedFileIdUseCase(private val repository: AutoBackupRepository) {

    suspend operator fun invoke(): String? = repository.getTrackedFileId()
}
