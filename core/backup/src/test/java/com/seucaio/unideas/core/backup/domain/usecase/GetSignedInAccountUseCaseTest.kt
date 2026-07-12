package com.seucaio.unideas.core.backup.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.repository.GoogleAuthRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetSignedInAccountUseCaseTest {

    private val repository: GoogleAuthRepository = mockk()
    private val useCase = GetSignedInAccountUseCase(repository)

    @Test
    fun `invoke delegates to the repository when connected`() {
        val account: GoogleSignInAccount = mockk()
        every { repository.getSignedInAccount() } returns account

        val result = useCase()

        assertEquals(account, result)
        verify(exactly = 1) { repository.getSignedInAccount() }
    }

    @Test
    fun `invoke returns null when disconnected`() {
        every { repository.getSignedInAccount() } returns null

        val result = useCase()

        assertNull(result)
    }
}
