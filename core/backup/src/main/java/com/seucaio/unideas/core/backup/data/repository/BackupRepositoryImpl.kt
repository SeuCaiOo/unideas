package com.seucaio.unideas.core.backup.data.repository

import android.content.Context
import com.google.api.client.http.FileContent
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.repository.BackupRepository
import com.seucaio.unideas.data.local.database.UnideasDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class BackupRepositoryImpl(
    private val database: UnideasDatabase,
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BackupRepository {

    override suspend fun uploadBackup(driveService: Drive): Result<BackupInfo> = runCatching {
        withContext(ioDispatcher) {
            UnideasDatabase.checkpoint(database)

            val dbFile = context.getDatabasePath(UnideasDatabase.DATABASE_NAME)
            val tempFile = java.io.File(context.cacheDir, TEMP_BACKUP_FILE_NAME)
            dbFile.copyTo(tempFile, overwrite = true)

            val metadata = File().apply {
                name = UnideasDatabase.DATABASE_NAME
                parents = listOf(APP_DATA_FOLDER)
            }

            val mediaContent = FileContent(MIME_SQLITE, tempFile)
            val uploaded = driveService.files().create(metadata, mediaContent)
                .setFields("id, name, size, createdTime")
                .execute()

            tempFile.delete()

            BackupInfo(
                fileId = uploaded.id,
                createdAt = uploaded.createdTime.toLocalDateTime(),
                sizeBytes = uploaded.getSize() ?: dbFile.length(),
            )
        }
    }

    override suspend fun listBackups(driveService: Drive): Result<List<BackupInfo>> = runCatching {
        withContext(ioDispatcher) {
            val result = driveService.files().list()
                .setSpaces(APP_DATA_FOLDER)
                .setFields("files(id, name, size, createdTime)")
                .setQ("name = '${UnideasDatabase.DATABASE_NAME}'")
                .setOrderBy("createdTime desc")
                .execute()

            result.files?.map { file ->
                BackupInfo(
                    fileId = file.id,
                    createdAt = file.createdTime.toLocalDateTime(),
                    sizeBytes = file.getSize() ?: 0L,
                )
            } ?: emptyList()
        }
    }

    override suspend fun restoreBackup(driveService: Drive, fileId: String): Result<Unit> =
        runCatching {
            withContext(ioDispatcher) {
                database.close()
                UnideasDatabase.resetInstance()

                val dbFile = context.getDatabasePath(UnideasDatabase.DATABASE_NAME)
                java.io.File("${dbFile.path}-wal").delete()
                java.io.File("${dbFile.path}-shm").delete()

                dbFile.outputStream().use { output ->
                    driveService.files().get(fileId).executeMediaAndDownloadTo(output)
                }

                UnideasDatabase.getInstance(context)
            }
        }

    private fun DateTime.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault())

    companion object {
        private const val APP_DATA_FOLDER = "appDataFolder"
        private const val MIME_SQLITE = "application/x-sqlite3"
        private const val TEMP_BACKUP_FILE_NAME = "temp_backup.db"
    }
}
