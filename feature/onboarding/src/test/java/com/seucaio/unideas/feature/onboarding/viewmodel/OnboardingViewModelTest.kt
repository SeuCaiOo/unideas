package com.seucaio.unideas.feature.onboarding.viewmodel

import android.content.Intent
import app.cash.turbine.test
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
import com.seucaio.unideas.core.backup.domain.usecase.GetSignInIntentUseCase
import com.seucaio.unideas.domain.usecase.onboarding.SetOnboardingSeenUseCase
import com.seucaio.unideas.feature.onboarding.R
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

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @MockK
    private lateinit var getSignInIntent: GetSignInIntentUseCase

    @MockK
    private lateinit var setOnboardingSeen: SetOnboardingSeenUseCase

    @MockK
    private lateinit var backupUseCase: BackupUseCase

    private val intent: Intent = mockk()
    private val account: GoogleSignInAccount = mockk()
    private val backupInfo: BackupInfo = mockk()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = OnboardingViewModel(getSignInIntent, setOnboardingSeen, backupUseCase)

    @Test
    fun `when created should expose Ready`() = runTest {
        val vm = viewModel()

        assertEquals(OnboardingUiState.Ready, vm.uiState.value)
    }

    @Test
    fun `when OnConnectClicked should launch the Google sign-in intent and expose Connecting`() = runTest {
        every { getSignInIntent() } returns intent
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(OnboardingEvent.OnConnectClicked)
            assertEquals(OnboardingUiAction.LaunchGoogleSignIn(intent), awaitItem())
        }
        assertEquals(OnboardingUiState.Connecting, vm.uiState.value)
    }

    @Test
    fun `when OnSkipClicked should mark onboarding seen and complete`() = runTest {
        coEvery { setOnboardingSeen(true) } returns Result.success(Unit)
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(OnboardingEvent.OnSkipClicked)
            assertEquals(OnboardingUiAction.OnboardingComplete, awaitItem())
        }
        coVerify(exactly = 1) { setOnboardingSeen(true) }
    }

    @Test
    fun `when OnGoogleSignInResult with an account and no backup should mark onboarding seen, complete and reset to Ready`() =
        runTest {
            coEvery { backupUseCase.getLastBackupInfo(account) } returns Result.success(null)
            coEvery { setOnboardingSeen(true) } returns Result.success(Unit)
            val vm = viewModel()

            vm.uiAction.test {
                vm.onEvent(OnboardingEvent.OnGoogleSignInResult(account))
                assertEquals(OnboardingUiAction.OnboardingComplete, awaitItem())
            }
            coVerify(exactly = 1) { setOnboardingSeen(true) }
            assertEquals(OnboardingUiState.Ready, vm.uiState.value)
        }

    @Test
    fun `when OnGoogleSignInResult with an account and a backup should show the restore sheet and reset to Ready`() =
        runTest {
            coEvery { backupUseCase.getLastBackupInfo(account) } returns Result.success(backupInfo)
            val vm = viewModel()

            vm.uiAction.test {
                vm.onEvent(OnboardingEvent.OnGoogleSignInResult(account))
                assertEquals(OnboardingUiAction.ShowRestoreBackupSheet(account, backupInfo), awaitItem())
            }
            coVerify(exactly = 0) { setOnboardingSeen(any()) }
            assertEquals(OnboardingUiState.Ready, vm.uiState.value)
        }

    @Test
    fun `when OnGoogleSignInResult with no account should show an error, not complete and reset to Ready`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(OnboardingEvent.OnGoogleSignInResult(null))
            assertEquals(OnboardingUiAction.ShowSnackbar(R.string.onboarding_signin_failed), awaitItem())
        }
        coVerify(exactly = 0) { setOnboardingSeen(any()) }
        assertEquals(OnboardingUiState.Ready, vm.uiState.value)
    }

    @Test
    fun `when OnRestoreBackupConfirmed succeeds should mark onboarding seen and signal restore completed`() = runTest {
        coEvery { backupUseCase.getLastBackupInfo(account) } returns Result.success(backupInfo)
        coEvery { backupUseCase.restore(account, "file-1") } returns Result.success(Unit)
        coEvery { setOnboardingSeen(true) } returns Result.success(Unit)
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(OnboardingEvent.OnGoogleSignInResult(account))
            awaitItem()
            vm.onEvent(OnboardingEvent.OnRestoreBackupConfirmed("file-1"))
            assertEquals(OnboardingUiAction.RestoreCompleted, awaitItem())
        }
        coVerify(exactly = 1) { setOnboardingSeen(true) }
    }

    @Test
    fun `when OnRestoreBackupConfirmed fails should show an error and not complete`() = runTest {
        coEvery { backupUseCase.getLastBackupInfo(account) } returns Result.success(backupInfo)
        coEvery { backupUseCase.restore(account, "file-1") } returns Result.failure(RuntimeException("boom"))
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(OnboardingEvent.OnGoogleSignInResult(account))
            awaitItem()
            vm.onEvent(OnboardingEvent.OnRestoreBackupConfirmed("file-1"))
            assertEquals(OnboardingUiAction.ShowSnackbar(R.string.onboarding_restore_failed), awaitItem())
        }
        coVerify(exactly = 0) { setOnboardingSeen(any()) }
    }

    @Test
    fun `when OnStartFreshClicked should mark onboarding seen and complete`() = runTest {
        coEvery { setOnboardingSeen(true) } returns Result.success(Unit)
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(OnboardingEvent.OnStartFreshClicked)
            assertEquals(OnboardingUiAction.OnboardingComplete, awaitItem())
        }
        coVerify(exactly = 1) { setOnboardingSeen(true) }
    }
}
