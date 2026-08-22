package com.seucaio.unideas.core.backup.viewmodel

import android.content.Intent
import app.cash.turbine.test
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.R
import com.seucaio.unideas.core.backup.domain.usecase.GoogleAuthUseCase
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
class AccountViewModelTest {

    @MockK
    private lateinit var googleAuthUseCase: GoogleAuthUseCase

    private val account: GoogleSignInAccount = mockk(relaxed = true)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { googleAuthUseCase.getSignedInAccount() } returns null
        every { account.displayName } returns "Current User"
        every { account.email } returns "current@example.com"
        coEvery { googleAuthUseCase.signOut() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = AccountViewModel(googleAuthUseCase)

    @Test
    fun `when created with no signed-in account should expose disconnected state`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(AccountUiState(isConnected = false), awaitItem())
        }
    }

    @Test
    fun `when created with a signed-in account should expose connected state with identity`() = runTest {
        every { googleAuthUseCase.getSignedInAccount() } returns account
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(
                AccountUiState(
                    isConnected = true,
                    accountName = "Current User",
                    accountEmail = "current@example.com",
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `when OnSignInClick should launch google sign-in`() = runTest {
        val intent: Intent = mockk()
        every { googleAuthUseCase.getSignInIntent() } returns intent
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(AccountEvent.OnSignInClick)
            assertEquals(AccountUiAction.LaunchGoogleSignIn(intent), awaitItem())
        }
    }

    @Test
    fun `when sign-in result is null should show the sign-in failed snackbar`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(AccountEvent.OnGoogleSignInResult(null))
            assertEquals(AccountUiAction.ShowSnackbar(R.string.backup_sign_in_failed), awaitItem())
        }
    }

    @Test
    fun `when sign-in result has an account should update identity and emit SignInCompleted`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(AccountUiState(isConnected = false), awaitItem())

            vm.uiAction.test {
                vm.onEvent(AccountEvent.OnGoogleSignInResult(account))
                assertEquals(AccountUiAction.SignInCompleted, awaitItem())
            }

            assertEquals(
                AccountUiState(
                    isConnected = true,
                    accountName = "Current User",
                    accountEmail = "current@example.com",
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `when OnSignOutClick should sign out and emit SignedOut`() = runTest {
        every { googleAuthUseCase.getSignedInAccount() } returns account
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(
                AccountUiState(
                    isConnected = true,
                    accountName = "Current User",
                    accountEmail = "current@example.com",
                ),
                awaitItem(),
            )

            vm.uiAction.test {
                vm.onEvent(AccountEvent.OnSignOutClick)
                assertEquals(AccountUiAction.SignedOut, awaitItem())
            }

            assertEquals(AccountUiState(isConnected = false), awaitItem())
        }
        coVerify(exactly = 1) { googleAuthUseCase.signOut() }
    }
}
