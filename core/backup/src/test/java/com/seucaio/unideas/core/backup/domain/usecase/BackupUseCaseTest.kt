package com.seucaio.unideas.core.backup.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/** [BackupUseCase] is a delegating facade — these tests only check the delegation itself. */
class BackupUseCaseTest {

    private val buildDriveServiceUseCase: BuildDriveServiceUseCase = mockk()
    private val uploadBackupUseCase: UploadBackupUseCase = mockk()
    private val listBackupsUseCase: ListBackupsUseCase = mockk()
    private val restoreBackupUseCase: RestoreBackupUseCase = mockk()
    private val getLastBackupInfoUseCase: GetLastBackupInfoUseCase = mockk()
    private val useCase = BackupUseCase(
        buildDriveServiceUseCase,
        uploadBackupUseCase,
        listBackupsUseCase,
        restoreBackupUseCase,
        getLastBackupInfoUseCase,
    )

    private val account: GoogleSignInAccount = mockk()
    private val driveService: Drive = mockk()

    @Before
    fun setUp() {
        every { buildDriveServiceUseCase(account) } returns driveService
    }

    @Test
    fun `upload builds the Drive service and delegates to UploadBackupUseCase`() = runTest {
        val info = BackupInfo("file-1", LocalDateTime.now(), 1024L)
        coEvery { uploadBackupUseCase(driveService) } returns Result.success(info)

        val result = useCase.upload(account)

        assertEquals(info, result.getOrNull())
        coVerify(exactly = 1) { uploadBackupUseCase(driveService) }
    }

    @Test
    fun `list builds the Drive service and delegates to ListBackupsUseCase`() = runTest {
        val backups = listOf(BackupInfo("file-1", LocalDateTime.now(), 1024L))
        coEvery { listBackupsUseCase(driveService) } returns Result.success(backups)

        val result = useCase.list(account)

        assertEquals(backups, result.getOrNull())
        coVerify(exactly = 1) { listBackupsUseCase(driveService) }
    }

    @Test
    fun `restore builds the Drive service and delegates to RestoreBackupUseCase`() = runTest {
        coEvery { restoreBackupUseCase(driveService, "file-1") } returns Result.success(Unit)

        val result = useCase.restore(account, "file-1")

        assertEquals(Result.success(Unit), result)
        coVerify(exactly = 1) { restoreBackupUseCase(driveService, "file-1") }
    }

    @Test
    fun `getLastBackupInfo builds the Drive service and delegates to GetLastBackupInfoUseCase`() = runTest {
        val info = BackupInfo("file-1", LocalDateTime.now(), 1024L)
        coEvery { getLastBackupInfoUseCase(driveService) } returns Result.success(info)

        val result = useCase.getLastBackupInfo(account)

        assertEquals(info, result.getOrNull())
        coVerify(exactly = 1) { getLastBackupInfoUseCase(driveService) }
    }
}
