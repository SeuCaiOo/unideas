package com.seucaio.unideas.core.backup.viewmodel

import app.cash.turbine.test
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.usecase.AutoBackupSettingsUseCase
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GetSignedInAccountUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
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
class BackupSyncViewModelTest {

    @MockK
    private lateinit var getSignedInAccountUseCase: GetSignedInAccountUseCase

    @MockK
    private lateinit var backupUseCase: BackupUseCase

    @MockK
    private lateinit var autoBackupSettingsUseCase: AutoBackupSettingsUseCase

    private val account: GoogleSignInAccount = mockk()
    private val remoteBackup = BackupInfo("remote-file", LocalDateTime.now(), 2048L)

    private lateinit var viewModel: BackupSyncViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { getSignedInAccountUseCase() } returns account
        viewModel = BackupSyncViewModel(getSignedInAccountUseCase, backupUseCase, autoBackupSettingsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when OnDesyncDetected should show the restore prompt`() = runTest {
        viewModel.onEvent(BackupSyncEvent.OnDesyncDetected(remoteBackup))

        assertEquals(BackupSyncDialogState.RestorePrompt(remoteBackup), viewModel.dialogState.value)
    }

    @Test
    fun `when OnRestoreDeclineClicked should show the disable-sync confirmation`() = runTest {
        viewModel.onEvent(BackupSyncEvent.OnDesyncDetected(remoteBackup))

        viewModel.onEvent(BackupSyncEvent.OnRestoreDeclineClicked)

        assertEquals(BackupSyncDialogState.DisableSyncConfirm(remoteBackup), viewModel.dialogState.value)
    }

    @Test
    fun `when OnDisableSyncDeclineClicked should go back to the restore prompt`() = runTest {
        viewModel.onEvent(BackupSyncEvent.OnDesyncDetected(remoteBackup))
        viewModel.onEvent(BackupSyncEvent.OnRestoreDeclineClicked)

        viewModel.onEvent(BackupSyncEvent.OnDisableSyncDeclineClicked)

        assertEquals(BackupSyncDialogState.RestorePrompt(remoteBackup), viewModel.dialogState.value)
    }

    @Test
    fun `when OnRestoreConfirmClicked succeeds should track the file id and dismiss`() = runTest {
        coEvery { backupUseCase.restore(account, "remote-file") } returns Result.success(Unit)
        coEvery { autoBackupSettingsUseCase.setTrackedFileId("remote-file") } returns Unit
        viewModel.onEvent(BackupSyncEvent.OnDesyncDetected(remoteBackup))

        viewModel.uiAction.test {
            viewModel.onEvent(BackupSyncEvent.OnRestoreConfirmClicked)
            assertEquals(BackupSyncUiAction.RestoreCompleted, awaitItem())
        }

        assertEquals(BackupSyncDialogState.None, viewModel.dialogState.value)
        coVerify(exactly = 1) { autoBackupSettingsUseCase.setTrackedFileId("remote-file") }
    }

    @Test
    fun `when OnRestoreConfirmClicked fails should emit an error and keep the dialog open`() = runTest {
        val error = RuntimeException("Network error")
        coEvery { backupUseCase.restore(account, "remote-file") } returns Result.failure(error)
        viewModel.onEvent(BackupSyncEvent.OnDesyncDetected(remoteBackup))

        viewModel.uiAction.test {
            viewModel.onEvent(BackupSyncEvent.OnRestoreConfirmClicked)
            assertEquals(BackupSyncUiAction.ShowError("Network error"), awaitItem())
        }

        assertEquals(BackupSyncDialogState.RestorePrompt(remoteBackup), viewModel.dialogState.value)
        coVerify(exactly = 0) { autoBackupSettingsUseCase.setTrackedFileId(any()) }
    }

    @Test
    fun `when OnDisableSyncConfirmClicked should turn off auto-backup and dismiss`() = runTest {
        coEvery { autoBackupSettingsUseCase.setEnabled(false) } returns Unit
        viewModel.onEvent(BackupSyncEvent.OnDesyncDetected(remoteBackup))
        viewModel.onEvent(BackupSyncEvent.OnRestoreDeclineClicked)

        viewModel.onEvent(BackupSyncEvent.OnDisableSyncConfirmClicked)

        assertEquals(BackupSyncDialogState.None, viewModel.dialogState.value)
        coVerify(exactly = 1) { autoBackupSettingsUseCase.setEnabled(false) }
    }
}
