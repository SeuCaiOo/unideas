package com.seucaio.unideas.core.backup.data.repository

import com.seucaio.unideas.core.backup.data.local.datastore.AutoBackupPreferences
import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository

class AutoBackupRepositoryImpl(
    private val preferences: AutoBackupPreferences,
) : AutoBackupRepository {

    override suspend fun isEnabled(): Boolean = preferences.isEnabled()

    override suspend fun setEnabled(enabled: Boolean) = preferences.setEnabled(enabled)

    override suspend fun getTrackedFileId(): String? = preferences.getTrackedFileId()

    override suspend fun setTrackedFileId(fileId: String?) = preferences.setTrackedFileId(fileId)
}
