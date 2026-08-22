package com.seucaio.unideas.core.backup.viewmodel

import android.content.Intent
import app.cash.turbine.test
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.R
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GoogleAuthUseCase
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
    private lateinit var googleAuthUseCase: GoogleAuthUseCase

    @MockK
    private lateinit var backupUseCase: BackupUseCase

    private val account: GoogleSignInAccount = mockk()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { googleAuthUseCase.getSignedInAccount() } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = BackupViewModel(googleAuthUseCase, backupUseCase)

    @Test
    fun `when created with no signed-in account should expose disconnected state`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(BackupUiState.Ready(isConnected = false), awaitItem())
        }
    }

    @Test
    fun `when created with a signed-in account should expose connected state with last backup`() = runTest {
        val createdAt = LocalDateTime.of(2026, 7, 12, 8, 30)
        every { googleAuthUseCase.getSignedInAccount() } returns account
        coEvery { backupUseCase.getLastBackupInfo(account) } returns
            Result.success(BackupInfo("file-1", createdAt, 1024L))

        val vm = viewModel()

        vm.uiState.test {
            assertEquals(BackupUiState.Ready(isConnected = true, lastBackupAt = createdAt), awaitItem())
        }
    }

    @Test
    fun `when created with a signed-in account and no prior backup should expose connected state`() = runTest {
        every { googleAuthUseCase.getSignedInAccount() } returns account
        coEvery { backupUseCase.getLastBackupInfo(account) } returns Result.success(null)

        val vm = viewModel()

        vm.uiState.test {
            assertEquals(BackupUiState.Ready(isConnected = true), awaitItem())
        }
    }

    @Test
    fun `when created with a signed-in account but the info fetch fails should silently expose disconnected`() =
        runTest {
            every { googleAuthUseCase.getSignedInAccount() } returns account
            coEvery { backupUseCase.getLastBackupInfo(account) } returns
                Result.failure(RuntimeException("error"))

            val vm = viewModel()

            vm.uiState.test {
                assertEquals(BackupUiState.Ready(isConnected = false), awaitItem())
            }
            vm.action.test {
                expectNoEvents()
            }
        }

    @Test
    fun `when OnConnectClick should launch sign-in for the connect action`() = runTest {
        val intent: Intent = mockk()
        every { googleAuthUseCase.getSignInIntent() } returns intent
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnConnectClick)
            assertEquals(BackupUiAction.LaunchGoogleSignIn(intent, BackupAction.Connect), awaitItem())
        }
    }

    @Test
    fun `when connect sign-in succeeds should expose connected state with the last backup`() = runTest {
        val createdAt = LocalDateTime.of(2026, 7, 12, 8, 30)
        coEvery { backupUseCase.getLastBackupInfo(account) } returns
            Result.success(BackupInfo("file-1", createdAt, 1024L))
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(BackupUiState.Ready(isConnected = false), awaitItem())

            vm.onEvent(BackupEvent.OnGoogleSignInResult(account, BackupAction.Connect))

            assertEquals(BackupUiState.Ready(isConnected = true, lastBackupAt = createdAt), awaitItem())
        }
    }

    @Test
    fun `when OnBackupClick should launch sign-in for the upload action`() = runTest {
        val intent: Intent = mockk()
        every { googleAuthUseCase.getSignInIntent() } returns intent
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnBackupClick)
            assertEquals(BackupUiAction.LaunchGoogleSignIn(intent, BackupAction.Upload), awaitItem())
        }
    }

    @Test
    fun `when OnToggleBackupListClick with the list hidden should launch sign-in for the sync action`() = runTest {
        val intent: Intent = mockk()
        every { googleAuthUseCase.getSignInIntent() } returns intent
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnToggleBackupListClick)
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
        coEvery { backupUseCase.upload(account) } returns
            Result.success(BackupInfo("file-1", createdAt, 1024L))
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(BackupUiState.Ready(), awaitItem())

            vm.action.test {
                vm.onEvent(BackupEvent.OnGoogleSignInResult(account, BackupAction.Upload))
                assertEquals(BackupUiAction.ShowSnackbar(R.string.backup_upload_success), awaitItem())
            }

            assertEquals(BackupUiState.Ready(isConnected = true, lastBackupAt = createdAt), awaitItem())
        }
    }

    @Test
    fun `when upload fails should show the error snackbar`() = runTest {
        coEvery { backupUseCase.upload(account) } returns Result.failure(RuntimeException("error"))
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnGoogleSignInResult(account, BackupAction.Upload))
            assertEquals(BackupUiAction.ShowSnackbar(R.string.backup_error), awaitItem())
        }
    }

    @Test
    fun `when sync finds no backups should show the not-found snackbar`() = runTest {
        coEvery { backupUseCase.list(account) } returns Result.success(emptyList())
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnGoogleSignInResult(account, BackupAction.Sync))
            assertEquals(BackupUiAction.ShowSnackbar(R.string.backup_no_backups_found), awaitItem())
        }
    }

    @Test
    fun `when sync finds backups should reveal the inline backup list`() = runTest {
        val backups = listOf(BackupInfo("file-1", LocalDateTime.now(), 1024L))
        coEvery { backupUseCase.list(account) } returns Result.success(backups)
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(BackupUiState.Ready(), awaitItem())

            vm.onEvent(BackupEvent.OnGoogleSignInResult(account, BackupAction.Sync))

            assertEquals(
                BackupUiState.Ready(isConnected = true, isBackupListVisible = true, availableBackups = backups),
                awaitItem(),
            )
        }
    }

    @Test
    fun `when OnToggleBackupListClick with the list visible should hide it and clear the selection`() = runTest {
        val backups = listOf(BackupInfo("file-1", LocalDateTime.now(), 1024L))
        coEvery { backupUseCase.list(account) } returns Result.success(backups)
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(BackupUiState.Ready(), awaitItem())

            vm.onEvent(BackupEvent.OnGoogleSignInResult(account, BackupAction.Sync))
            awaitItem()

            vm.onEvent(BackupEvent.OnBackupSelected("file-1"))
            awaitItem()

            vm.onEvent(BackupEvent.OnToggleBackupListClick)

            assertEquals(
                BackupUiState.Ready(isConnected = true, availableBackups = backups),
                awaitItem(),
            )
        }
    }

    @Test
    fun `when sync fails should show the error snackbar`() = runTest {
        coEvery { backupUseCase.list(account) } returns Result.failure(RuntimeException("error"))
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnGoogleSignInResult(account, BackupAction.Sync))
            assertEquals(BackupUiAction.ShowSnackbar(R.string.backup_error), awaitItem())
        }
    }

    @Test
    fun `when OnBackupSelected should update the selected backup id`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(BackupUiState.Ready(), awaitItem())

            vm.onEvent(BackupEvent.OnBackupSelected("file-1"))

            assertEquals(BackupUiState.Ready(selectedBackupFileId = "file-1"), awaitItem())
        }
    }

    @Test
    fun `when OnRestoreClick with no selection should do nothing`() = runTest {
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnRestoreClick)
            expectNoEvents()
        }
    }

    @Test
    fun `when OnRestoreClick with a selection succeeds should emit RestoreCompleted`() = runTest {
        every { googleAuthUseCase.getSignedInAccount() } returns account
        coEvery { backupUseCase.getLastBackupInfo(account) } returns Result.success(null)
        coEvery { backupUseCase.restore(account, "file-1") } returns Result.success(Unit)
        val vm = viewModel()
        vm.onEvent(BackupEvent.OnBackupSelected("file-1"))

        vm.action.test {
            vm.onEvent(BackupEvent.OnRestoreClick)
            assertEquals(BackupUiAction.RestoreCompleted, awaitItem())
        }
    }

    @Test
    fun `when OnRestoreClick with a selection fails should show the error snackbar`() = runTest {
        every { googleAuthUseCase.getSignedInAccount() } returns account
        coEvery { backupUseCase.getLastBackupInfo(account) } returns Result.success(null)
        coEvery { backupUseCase.restore(account, "file-1") } returns
            Result.failure(RuntimeException("error"))
        val vm = viewModel()
        vm.onEvent(BackupEvent.OnBackupSelected("file-1"))

        vm.action.test {
            vm.onEvent(BackupEvent.OnRestoreClick)
            assertEquals(BackupUiAction.ShowSnackbar(R.string.backup_error), awaitItem())
        }
    }

    @Test
    fun `when OnDeleteBackupClick should ask for confirmation`() = runTest {
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnDeleteBackupClick("file-1"))
            assertEquals(BackupUiAction.ShowDeleteConfirm("file-1"), awaitItem())
        }
    }

    @Test
    fun `when OnDeleteConfirmed with no signed-in account should do nothing`() = runTest {
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnDeleteConfirmed("file-1"))
            expectNoEvents()
        }
    }

    @Test
    fun `when OnDeleteConfirmed succeeds should remove the backup and show the success snackbar`() = runTest {
        val backups = listOf(
            BackupInfo("file-1", LocalDateTime.now(), 1024L),
            BackupInfo("file-2", LocalDateTime.now(), 1024L)
        )
        every { googleAuthUseCase.getSignedInAccount() } returns account
        coEvery { backupUseCase.getLastBackupInfo(account) } returns Result.success(null)
        coEvery { backupUseCase.list(account) } returns Result.success(backups)
        coEvery { backupUseCase.delete(account, "file-2") } returns Result.success(Unit)
        val vm = viewModel()
        vm.onEvent(BackupEvent.OnGoogleSignInResult(account, BackupAction.Sync))
        vm.onEvent(BackupEvent.OnBackupSelected("file-2"))

        vm.uiState.test {
            assertEquals(
                BackupUiState.Ready(
                    isConnected = true,
                    isBackupListVisible = true,
                    availableBackups = backups,
                    selectedBackupFileId = "file-2",
                ),
                awaitItem(),
            )

            vm.action.test {
                vm.onEvent(BackupEvent.OnDeleteConfirmed("file-2"))
                assertEquals(BackupUiAction.ShowSnackbar(R.string.backup_delete_success), awaitItem())
            }

            assertEquals(
                BackupUiState.Ready(
                    isConnected = true,
                    isBackupListVisible = true,
                    availableBackups = listOf(backups[0]),
                    selectedBackupFileId = null,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `when OnDeleteConfirmed fails should show the error snackbar`() = runTest {
        every { googleAuthUseCase.getSignedInAccount() } returns account
        coEvery { backupUseCase.getLastBackupInfo(account) } returns Result.success(null)
        coEvery { backupUseCase.delete(account, "file-1") } returns
            Result.failure(RuntimeException("error"))
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(BackupEvent.OnDeleteConfirmed("file-1"))
            assertEquals(BackupUiAction.ShowSnackbar(R.string.backup_delete_error), awaitItem())
        }
    }
}
