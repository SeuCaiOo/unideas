package com.seucaio.unideas.core.backup.domain.repository

/**
 * Automatic-backup preference and the Drive `fileId` currently tracked as its single slot —
 * separate from [BackupRepository], which only knows how to move the Room DB file to/from Drive
 * and has no notion of "which upload was automatic".
 */
interface AutoBackupRepository {

    suspend fun isEnabled(): Boolean

    suspend fun setEnabled(enabled: Boolean)

    suspend fun getTrackedFileId(): String?

    suspend fun setTrackedFileId(fileId: String?)
}
