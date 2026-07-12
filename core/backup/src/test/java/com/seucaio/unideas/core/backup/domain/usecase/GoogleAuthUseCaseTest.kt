package com.seucaio.unideas.core.backup.domain.usecase

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

/** [GoogleAuthUseCase] is a delegating facade — these tests only check the delegation itself. */
class GoogleAuthUseCaseTest {

    private val getSignInIntentUseCase: GetSignInIntentUseCase = mockk()
    private val getSignedInAccountUseCase: GetSignedInAccountUseCase = mockk()
    private val useCase = GoogleAuthUseCase(getSignInIntentUseCase, getSignedInAccountUseCase)

    private val account: GoogleSignInAccount = mockk()

    @Test
    fun `getSignInIntent delegates to GetSignInIntentUseCase`() {
        val intent: Intent = mockk()
        every { getSignInIntentUseCase() } returns intent

        val result = useCase.getSignInIntent()

        assertEquals(intent, result)
        verify(exactly = 1) { getSignInIntentUseCase() }
    }

    @Test
    fun `getSignedInAccount delegates to GetSignedInAccountUseCase`() {
        every { getSignedInAccountUseCase() } returns account

        val result = useCase.getSignedInAccount()

        assertEquals(account, result)
        verify(exactly = 1) { getSignedInAccountUseCase() }
    }
}
