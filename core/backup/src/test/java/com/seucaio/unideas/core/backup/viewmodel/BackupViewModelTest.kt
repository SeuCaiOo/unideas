package com.seucaio.unideas.core.backup.viewmodel

import android.content.Intent
import app.cash.turbine.test
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.R
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {

    @MockK
    private lateinit var backupUseCase: BackupUseCase

    private val driveService: Drive = mockk()
    private val account: GoogleSignInAccount = mockk()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { backupUseCase.buildDriveService(account) } returns driveService
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = BackupViewModel(backupUseCase)

    @Test
    fun `when OnBackupClick should launch sign-in for the upload action`() = runTest {
        val intent: Intent = mockk()
        every { backupUseCase.getSignInIntent() } returns intent
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnBackupClick)
            assertEquals(BackupUiAction.LaunchGoogleSignIn(intent, BackupAction.Upload), awaitItem())
        }
    }

    @Test
    fun `when OnSyncClick should launch sign-in for the sync action`() = runTest {
        val intent: Intent = mockk()
        every { backupUseCase.getSignInIntent() } returns intent
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnSyncClick)
            assertEquals(BackupUiAction.LaunchGoogleSignIn(intent, BackupAction.Sync), awaitItem())
        }
    }

    @Test
    fun `when sign-in fails should show the sign-in failed snackbar`() = runTest {
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnGoogleSignInResult(null, BackupAction.Upload))
            assertEquals(BackupUiAction.ShowSnackbar(R.string.backup_sign_in_failed), awaitItem())
        }
    }

    @Test
    fun `when upload succeeds should update lastBackupAt and show the success snackbar`() = runTest {
        val createdAt = LocalDateTime.of(2026, 7, 12, 8, 30)
        coEvery { backupUseCase.upload(driveService) } returns
            Result.success(BackupInfo("file-1", createdAt, 1024L))
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(BackupUiState.Ready(), awaitItem())

            vm.action.test {
                vm.onEvent(BackupEvent.OnGoogleSignInResult(account, BackupAction.Upload))
                assertEquals(BackupUiAction.ShowSnackbar(R.string.backup_upload_success), awaitItem())
            }

            assertEquals(BackupUiState.Ready(lastBackupAt = createdAt), awaitItem())
        }
    }

    @Test
    fun `when upload fails should show the error snackbar`() = runTest {
        coEvery { backupUseCase.upload(driveService) } returns Result.failure(RuntimeException("error"))
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnGoogleSignInResult(account, BackupAction.Upload))
            assertEquals(BackupUiAction.ShowSnackbar(R.string.backup_error), awaitItem())
        }
    }

    @Test
    fun `when sync finds no backups should show the not-found snackbar`() = runTest {
        coEvery { backupUseCase.list(driveService) } returns Result.success(emptyList())
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnGoogleSignInResult(account, BackupAction.Sync))
            assertEquals(BackupUiAction.ShowSnackbar(R.string.backup_no_backups_found), awaitItem())
        }
    }

    @Test
    fun `when sync finds backups should show the restore dialog`() = runTest {
        val backups = listOf(BackupInfo("file-1", LocalDateTime.now(), 1024L))
        coEvery { backupUseCase.list(driveService) } returns Result.success(backups)
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnGoogleSignInResult(account, BackupAction.Sync))
            assertEquals(BackupUiAction.ShowRestoreDialog(backups), awaitItem())
        }
    }

    @Test
    fun `when sync fails should show the error snackbar`() = runTest {
        coEvery { backupUseCase.list(driveService) } returns Result.failure(RuntimeException("error"))
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnGoogleSignInResult(account, BackupAction.Sync))
            assertEquals(BackupUiAction.ShowSnackbar(R.string.backup_error), awaitItem())
        }
    }

    @Test
    fun `when restore succeeds should show the restore success snackbar`() = runTest {
        coEvery { backupUseCase.restore(driveService, "file-1") } returns Result.success(Unit)
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnRestoreConfirmed(account, "file-1"))
            assertEquals(BackupUiAction.ShowSnackbar(R.string.backup_restore_success), awaitItem())
        }
    }

    @Test
    fun `when restore fails should show the error snackbar`() = runTest {
        coEvery { backupUseCase.restore(driveService, "file-1") } returns
            Result.failure(RuntimeException("error"))
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnRestoreConfirmed(account, "file-1"))
            assertEquals(BackupUiAction.ShowSnackbar(R.string.backup_error), awaitItem())
        }
    }
}
