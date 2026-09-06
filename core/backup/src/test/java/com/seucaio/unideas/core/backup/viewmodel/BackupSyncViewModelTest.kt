package com.seucaio.unideas.core.backup.viewmodel

import app.cash.turbine.test
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.usecase.AutoBackupSettingsUseCase
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GetConfirmedBackupSyncStateUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GetSignedInAccountUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class BackupSyncViewModelTest {

    @MockK
    private lateinit var getSignedInAccountUseCase: GetSignedInAccountUseCase

    @MockK
    private lateinit var backupUseCase: BackupUseCase

    @MockK
    private lateinit var autoBackupSettingsUseCase: AutoBackupSettingsUseCase

    @MockK
    private lateinit var getConfirmedBackupSyncStateUseCase: GetConfirmedBackupSyncStateUseCase

    private val account: GoogleSignInAccount = mockk()
    private val remoteBackup = BackupInfo("remote-file", LocalDateTime.now(), 2048L)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: BackupSyncViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        every { getSignedInAccountUseCase() } returns account
        viewModel = BackupSyncViewModel(
            getSignedInAccountUseCase,
            backupUseCase,
            autoBackupSettingsUseCase,
            getConfirmedBackupSyncStateUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.desyncFound() {
        coEvery { autoBackupSettingsUseCase.isEnabled() } returns true
        coEvery { getConfirmedBackupSyncStateUseCase(account) } returns remoteBackup
        viewModel.onEvent(BackupSyncEvent.OnSyncCheckRequested)
        advanceUntilIdle()
    }

    @Test
    fun `when OnSyncCheckRequested finds a desync should show the restore prompt`() = runTest(testDispatcher) {
        desyncFound()

        assertEquals(BackupSyncDialogState.RestorePrompt(remoteBackup), viewModel.dialogState.value)
    }

    @Test
    fun `when OnSyncCheckRequested finds no desync should not show any prompt`() = runTest(testDispatcher) {
        coEvery { autoBackupSettingsUseCase.isEnabled() } returns true
        coEvery { getConfirmedBackupSyncStateUseCase(account) } returns null

        viewModel.onEvent(BackupSyncEvent.OnSyncCheckRequested)

        assertEquals(BackupSyncDialogState.None, viewModel.dialogState.value)
    }

    @Test
    fun `when OnSyncCheckRequested runs with auto-backup disabled should skip the check`() = runTest(testDispatcher) {
        coEvery { autoBackupSettingsUseCase.isEnabled() } returns false

        viewModel.onEvent(BackupSyncEvent.OnSyncCheckRequested)

        assertEquals(BackupSyncDialogState.None, viewModel.dialogState.value)
        coVerify(exactly = 0) { getConfirmedBackupSyncStateUseCase(any()) }
    }

    @Test
    fun `when OnSyncCheckRequested runs with no signed-in account should skip the check`() = runTest(testDispatcher) {
        coEvery { autoBackupSettingsUseCase.isEnabled() } returns true
        every { getSignedInAccountUseCase() } returns null

        viewModel.onEvent(BackupSyncEvent.OnSyncCheckRequested)

        assertEquals(BackupSyncDialogState.None, viewModel.dialogState.value)
        coVerify(exactly = 0) { getConfirmedBackupSyncStateUseCase(any()) }
    }

    @Test
    fun `when OnSyncCheckRequested resolves should mark the check as completed`() = runTest(testDispatcher) {
        coEvery { autoBackupSettingsUseCase.isEnabled() } returns false

        viewModel.onEvent(BackupSyncEvent.OnSyncCheckRequested)

        assertEquals(true, viewModel.syncCheckCompleted.value)
    }

    @Test
    fun `when OnRestoreDeclineClicked should show the disable-sync confirmation`() = runTest(testDispatcher) {
        desyncFound()

        viewModel.onEvent(BackupSyncEvent.OnRestoreDeclineClicked)

        assertEquals(BackupSyncDialogState.DisableSyncConfirm(remoteBackup), viewModel.dialogState.value)
    }

    @Test
    fun `when OnDisableSyncDeclineClicked should go back to the restore prompt`() = runTest(testDispatcher) {
        desyncFound()
        viewModel.onEvent(BackupSyncEvent.OnRestoreDeclineClicked)

        viewModel.onEvent(BackupSyncEvent.OnDisableSyncDeclineClicked)

        assertEquals(BackupSyncDialogState.RestorePrompt(remoteBackup), viewModel.dialogState.value)
    }

    @Test
    fun `when OnRestoreConfirmClicked succeeds should track the file id and dismiss`() = runTest(testDispatcher) {
        coEvery { backupUseCase.restore(account, "remote-file") } returns Result.success(Unit)
        coEvery { autoBackupSettingsUseCase.setTrackedFileId("remote-file") } returns Unit
        desyncFound()

        viewModel.uiAction.test {
            viewModel.onEvent(BackupSyncEvent.OnRestoreConfirmClicked)
            assertEquals(BackupSyncUiAction.RestoreCompleted, awaitItem())
        }

        assertEquals(BackupSyncDialogState.None, viewModel.dialogState.value)
        coVerify(exactly = 1) { autoBackupSettingsUseCase.setTrackedFileId("remote-file") }
    }

    @Test
    fun `when OnRestoreConfirmClicked fails should emit an error and keep the dialog open`() = runTest(testDispatcher) {
        val error = RuntimeException("Network error")
        coEvery { backupUseCase.restore(account, "remote-file") } returns Result.failure(error)
        desyncFound()

        viewModel.uiAction.test {
            viewModel.onEvent(BackupSyncEvent.OnRestoreConfirmClicked)
            assertEquals(BackupSyncUiAction.ShowError("Network error"), awaitItem())
        }

        assertEquals(BackupSyncDialogState.RestorePrompt(remoteBackup), viewModel.dialogState.value)
        coVerify(exactly = 0) { autoBackupSettingsUseCase.setTrackedFileId(any()) }
    }

    @Test
    fun `when OnRestoreConfirmClicked fires again while already restoring should not call restore twice`() =
        runTest(testDispatcher) {
            val restoreDeferred = CompletableDeferred<Result<Unit>>()
            coEvery { backupUseCase.restore(account, "remote-file") } coAnswers { restoreDeferred.await() }
            coEvery { autoBackupSettingsUseCase.setTrackedFileId("remote-file") } returns Unit
            desyncFound()

            viewModel.onEvent(BackupSyncEvent.OnRestoreConfirmClicked)
            assertEquals(
                true,
                (viewModel.dialogState.value as BackupSyncDialogState.RestorePrompt).isRestoring,
            )
            viewModel.onEvent(BackupSyncEvent.OnRestoreConfirmClicked)

            coVerify(exactly = 1) { backupUseCase.restore(any(), any()) }
            restoreDeferred.complete(Result.success(Unit))
        }

    @Test
    fun `when OnDisableSyncConfirmClicked should turn off auto-backup and dismiss`() = runTest(testDispatcher) {
        coEvery { autoBackupSettingsUseCase.setEnabled(false) } returns Unit
        desyncFound()
        viewModel.onEvent(BackupSyncEvent.OnRestoreDeclineClicked)

        viewModel.onEvent(BackupSyncEvent.OnDisableSyncConfirmClicked)

        assertEquals(BackupSyncDialogState.None, viewModel.dialogState.value)
        coVerify(exactly = 1) { autoBackupSettingsUseCase.setEnabled(false) }
    }
}
