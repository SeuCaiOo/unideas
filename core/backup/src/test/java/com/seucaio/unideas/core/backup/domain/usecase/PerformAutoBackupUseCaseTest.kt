package com.seucaio.unideas.core.backup.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.repository.AutoBackupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class PerformAutoBackupUseCaseTest {

    private val autoBackupRepository: AutoBackupRepository = mockk()
    private val googleAuthUseCase: GoogleAuthUseCase = mockk()
    private val backupUseCase: BackupUseCase = mockk()
    private val useCase = PerformAutoBackupUseCase(autoBackupRepository, googleAuthUseCase, backupUseCase)

    private val account: GoogleSignInAccount = mockk()
    private val uploaded = BackupInfo("new-file", LocalDateTime.now(), 2048L)

    @Test
    fun `invoke is a no-op when the preference is off`() = runTest {
        coEvery { autoBackupRepository.isEnabled() } returns false

        val result = useCase()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { googleAuthUseCase.getSignedInAccount() }
        coVerify(exactly = 0) { backupUseCase.upload(any()) }
    }

    @Test
    fun `invoke is a no-op when no account is connected`() = runTest {
        coEvery { autoBackupRepository.isEnabled() } returns true
        every { googleAuthUseCase.getSignedInAccount() } returns null

        val result = useCase()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { backupUseCase.upload(any()) }
    }

    @Test
    fun `invoke uploads and tracks the new file id when there is no previous slot`() = runTest {
        coEvery { autoBackupRepository.isEnabled() } returns true
        every { googleAuthUseCase.getSignedInAccount() } returns account
        coEvery { autoBackupRepository.getTrackedFileId() } returns null
        coEvery { backupUseCase.upload(account) } returns Result.success(uploaded)
        coEvery { autoBackupRepository.setTrackedFileId("new-file") } returns Unit

        val result = useCase()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { autoBackupRepository.setTrackedFileId("new-file") }
        coVerify(exactly = 0) { backupUseCase.delete(any(), any()) }
    }

    @Test
    fun `invoke uploads, tracks the new file id, and deletes the previous slot`() = runTest {
        coEvery { autoBackupRepository.isEnabled() } returns true
        every { googleAuthUseCase.getSignedInAccount() } returns account
        coEvery { autoBackupRepository.getTrackedFileId() } returns "old-file"
        coEvery { backupUseCase.upload(account) } returns Result.success(uploaded)
        coEvery { autoBackupRepository.setTrackedFileId("new-file") } returns Unit
        coEvery { backupUseCase.delete(account, "old-file") } returns Result.success(Unit)

        val result = useCase()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { autoBackupRepository.setTrackedFileId("new-file") }
        coVerify(exactly = 1) { backupUseCase.delete(account, "old-file") }
    }

    @Test
    fun `invoke fails when the upload fails and does not delete the previous slot`() = runTest {
        coEvery { autoBackupRepository.isEnabled() } returns true
        every { googleAuthUseCase.getSignedInAccount() } returns account
        coEvery { autoBackupRepository.getTrackedFileId() } returns "old-file"
        coEvery { backupUseCase.upload(account) } returns Result.failure(RuntimeException("IO error"))

        val result = useCase()

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { autoBackupRepository.setTrackedFileId(any()) }
        coVerify(exactly = 0) { backupUseCase.delete(any(), any()) }
    }

    @Test
    fun `invoke still succeeds when deleting the previous slot fails`() = runTest {
        coEvery { autoBackupRepository.isEnabled() } returns true
        every { googleAuthUseCase.getSignedInAccount() } returns account
        coEvery { autoBackupRepository.getTrackedFileId() } returns "old-file"
        coEvery { backupUseCase.upload(account) } returns Result.success(uploaded)
        coEvery { autoBackupRepository.setTrackedFileId("new-file") } returns Unit
        coEvery { backupUseCase.delete(account, "old-file") } returns Result.failure(RuntimeException("IO error"))

        val result = useCase()

        assertTrue(result.isSuccess)
    }
}
