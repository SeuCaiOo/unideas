package com.seucaio.unideas.core.backup.domain.usecase

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

/** [BackupUseCase] is a delegating facade — these tests only check the delegation itself. */
class BackupUseCaseTest {

    private val getSignInIntentUseCase: GetSignInIntentUseCase = mockk()
    private val buildDriveServiceUseCase: BuildDriveServiceUseCase = mockk()
    private val uploadBackupUseCase: UploadBackupUseCase = mockk()
    private val listBackupsUseCase: ListBackupsUseCase = mockk()
    private val restoreBackupUseCase: RestoreBackupUseCase = mockk()
    private val getLastBackupInfoUseCase: GetLastBackupInfoUseCase = mockk()
    private val useCase = BackupUseCase(
        getSignInIntentUseCase,
        buildDriveServiceUseCase,
        uploadBackupUseCase,
        listBackupsUseCase,
        restoreBackupUseCase,
        getLastBackupInfoUseCase,
    )

    private val account: GoogleSignInAccount = mockk()
    private val driveService: Drive = mockk()

    @Test
    fun `getSignInIntent delegates to GetSignInIntentUseCase`() {
        val intent: Intent = mockk()
        every { getSignInIntentUseCase() } returns intent

        val result = useCase.getSignInIntent()

        assertEquals(intent, result)
        verify(exactly = 1) { getSignInIntentUseCase() }
    }

    @Test
    fun `buildDriveService delegates to BuildDriveServiceUseCase`() {
        every { buildDriveServiceUseCase(account) } returns driveService

        val result = useCase.buildDriveService(account)

        assertEquals(driveService, result)
        verify(exactly = 1) { buildDriveServiceUseCase(account) }
    }

    @Test
    fun `upload delegates to UploadBackupUseCase`() = runTest {
        val info = BackupInfo("file-1", LocalDateTime.now(), 1024L)
        coEvery { uploadBackupUseCase(driveService) } returns Result.success(info)

        val result = useCase.upload(driveService)

        assertEquals(info, result.getOrNull())
        coVerify(exactly = 1) { uploadBackupUseCase(driveService) }
    }

    @Test
    fun `list delegates to ListBackupsUseCase`() = runTest {
        val backups = listOf(BackupInfo("file-1", LocalDateTime.now(), 1024L))
        coEvery { listBackupsUseCase(driveService) } returns Result.success(backups)

        val result = useCase.list(driveService)

        assertEquals(backups, result.getOrNull())
        coVerify(exactly = 1) { listBackupsUseCase(driveService) }
    }

    @Test
    fun `restore delegates to RestoreBackupUseCase`() = runTest {
        coEvery { restoreBackupUseCase(driveService, "file-1") } returns Result.success(Unit)

        val result = useCase.restore(driveService, "file-1")

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { restoreBackupUseCase(driveService, "file-1") }
    }

    @Test
    fun `getLastBackupInfo delegates to GetLastBackupInfoUseCase`() = runTest {
        val info = BackupInfo("file-1", LocalDateTime.now(), 1024L)
        coEvery { getLastBackupInfoUseCase(driveService) } returns Result.success(info)

        val result = useCase.getLastBackupInfo(driveService)

        assertEquals(info, result.getOrNull())
        coVerify(exactly = 1) { getLastBackupInfoUseCase(driveService) }
    }
}
