package com.seucaio.unideas.core.backup.viewmodel

import android.content.Intent
import app.cash.turbine.test
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.R
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GoogleAuthUseCase
import com.seucaio.unideas.domain.usecase.onboarding.SetOnboardingSeenUseCase
import com.seucaio.unideas.domain.usecase.settings.ClearDatabaseUseCase
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
class AccountViewModelTest {

    @MockK
    private lateinit var googleAuthUseCase: GoogleAuthUseCase

    @MockK
    private lateinit var backupUseCase: BackupUseCase

    @MockK
    private lateinit var clearDatabaseUseCase: ClearDatabaseUseCase

    @MockK
    private lateinit var setOnboardingSeenUseCase: SetOnboardingSeenUseCase

    private val account: GoogleSignInAccount = mockk(relaxed = true)
    private val newAccount: GoogleSignInAccount = mockk(relaxed = true)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { googleAuthUseCase.getSignedInAccount() } returns null
        every { account.displayName } returns "Current User"
        every { account.email } returns "current@example.com"
        every { newAccount.displayName } returns "New User"
        every { newAccount.email } returns "new@example.com"
        coEvery { clearDatabaseUseCase() } returns Unit
        coEvery { setOnboardingSeenUseCase(any()) } returns Result.success(Unit)
        coEvery { googleAuthUseCase.signOut() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = AccountViewModel(
        googleAuthUseCase,
        backupUseCase,
        clearDatabaseUseCase,
        setOnboardingSeenUseCase,
    )

    @Test
    fun `when created with no signed-in account should expose disconnected state`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(AccountUiState.Ready(isConnected = false), awaitItem())
        }
    }

    @Test
    fun `when created with a signed-in account should expose connected state with identity`() = runTest {
        every { googleAuthUseCase.getSignedInAccount() } returns account
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(
                AccountUiState.Ready(
                    isConnected = true,
                    accountName = "Current User",
                    accountEmail = "current@example.com",
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `when OnSwitchAccountClick should show the switch account dialog`() = runTest {
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(AccountEvent.OnSwitchAccountClick)
            assertEquals(AccountUiAction.ShowSwitchAccountDialog, awaitItem())
        }
    }

    @Test
    fun `when switch account confirmed should upload the current account and launch sign-in`() = runTest {
        every { googleAuthUseCase.getSignedInAccount() } returns account
        coEvery { backupUseCase.upload(account) } returns
            Result.success(BackupInfo("file-1", LocalDateTime.now(), 1024L))
        val intent: Intent = mockk()
        every { googleAuthUseCase.getSignInIntent() } returns intent
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(AccountEvent.OnSwitchAccountConfirmed)
            assertEquals(AccountUiAction.LaunchSwitchAccountSignIn(intent), awaitItem())
        }
        coVerify(exactly = 1) { backupUseCase.upload(account) }
    }

    @Test
    fun `when switch account current-account upload fails should still launch sign-in`() = runTest {
        every { googleAuthUseCase.getSignedInAccount() } returns account
        coEvery { backupUseCase.upload(account) } returns Result.failure(RuntimeException("error"))
        val intent: Intent = mockk()
        every { googleAuthUseCase.getSignInIntent() } returns intent
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(AccountEvent.OnSwitchAccountConfirmed)
            assertEquals(AccountUiAction.LaunchSwitchAccountSignIn(intent), awaitItem())
        }
    }

    @Test
    fun `when switch account sign-in fails should show the sign-in failed snackbar`() = runTest {
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(AccountEvent.OnSwitchAccountGoogleSignInResult(null))
            assertEquals(AccountUiAction.ShowSnackbar(R.string.backup_sign_in_failed), awaitItem())
        }
    }

    @Test
    fun `when switch account sign-in result has a backup should show the restore sheet`() = runTest {
        val backupInfo = BackupInfo("file-2", LocalDateTime.now(), 2048L)
        coEvery { backupUseCase.getLastBackupInfo(newAccount) } returns Result.success(backupInfo)
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(AccountEvent.OnSwitchAccountGoogleSignInResult(newAccount))
            assertEquals(AccountUiAction.ShowSwitchAccountRestoreSheet(backupInfo), awaitItem())
        }
    }

    @Test
    fun `when switch account has no backup should clear local data and complete without restart`() = runTest {
        coEvery { backupUseCase.getLastBackupInfo(newAccount) } returns Result.success(null)
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(AccountEvent.OnSwitchAccountGoogleSignInResult(newAccount))
            assertEquals(AccountUiAction.SwitchAccountCompleted(restarted = false), awaitItem())
        }
        coVerify(exactly = 1) { clearDatabaseUseCase() }
    }

    @Test
    fun `when switch account restore is confirmed should restore and complete with restart`() = runTest {
        val backupInfo = BackupInfo("file-2", LocalDateTime.now(), 2048L)
        coEvery { backupUseCase.getLastBackupInfo(newAccount) } returns Result.success(backupInfo)
        coEvery { backupUseCase.restore(newAccount, "file-2") } returns Result.success(Unit)
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(AccountEvent.OnSwitchAccountGoogleSignInResult(newAccount))
            awaitItem()

            vm.onEvent(AccountEvent.OnSwitchAccountRestoreConfirmed("file-2"))
            assertEquals(AccountUiAction.SwitchAccountCompleted(restarted = true), awaitItem())
        }
        coVerify(exactly = 0) { clearDatabaseUseCase() }
    }

    @Test
    fun `when switch account restore fails should show the error snackbar`() = runTest {
        val backupInfo = BackupInfo("file-2", LocalDateTime.now(), 2048L)
        coEvery { backupUseCase.getLastBackupInfo(newAccount) } returns Result.success(backupInfo)
        coEvery { backupUseCase.restore(newAccount, "file-2") } returns
            Result.failure(RuntimeException("error"))
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(AccountEvent.OnSwitchAccountGoogleSignInResult(newAccount))
            awaitItem()

            vm.onEvent(AccountEvent.OnSwitchAccountRestoreConfirmed("file-2"))
            assertEquals(AccountUiAction.ShowSnackbar(R.string.backup_error), awaitItem())
        }
    }

    @Test
    fun `when switch account start fresh is confirmed should clear local data and complete without restart`() =
        runTest {
            val backupInfo = BackupInfo("file-2", LocalDateTime.now(), 2048L)
            coEvery { backupUseCase.getLastBackupInfo(newAccount) } returns Result.success(backupInfo)
            val vm = viewModel()

            vm.action.test {
                vm.onEvent(AccountEvent.OnSwitchAccountGoogleSignInResult(newAccount))
                awaitItem()

                vm.onEvent(AccountEvent.OnSwitchAccountStartFreshConfirmed)
                assertEquals(AccountUiAction.SwitchAccountCompleted(restarted = false), awaitItem())
            }
            coVerify(exactly = 1) { clearDatabaseUseCase() }
        }

    @Test
    fun `when OnLogoutClick should show the logout dialog`() = runTest {
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(AccountEvent.OnLogoutClick)
            assertEquals(AccountUiAction.ShowLogoutDialog, awaitItem())
        }
    }

    @Test
    fun `when logout confirmed should sign out reset onboarding flag and emit LogoutCompleted`() = runTest {
        every { googleAuthUseCase.getSignedInAccount() } returns account
        coEvery { backupUseCase.upload(account) } returns
            Result.success(BackupInfo("file-1", LocalDateTime.now(), 1024L))
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(AccountEvent.OnLogoutConfirmed)
            assertEquals(AccountUiAction.LogoutCompleted, awaitItem())
        }
        coVerify(exactly = 1) { googleAuthUseCase.signOut() }
        coVerify(exactly = 1) { setOnboardingSeenUseCase(false) }
    }

    @Test
    fun `when logout confirmed with no connected account should still sign out and complete`() = runTest {
        val vm = viewModel()

        vm.action.test {
            vm.onEvent(AccountEvent.OnLogoutConfirmed)
            assertEquals(AccountUiAction.LogoutCompleted, awaitItem())
        }
        coVerify(exactly = 0) { backupUseCase.upload(any()) }
        coVerify(exactly = 1) { googleAuthUseCase.signOut() }
    }
}
