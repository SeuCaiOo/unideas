package com.seucaio.unideas.core.backup.domain.usecase

import android.content.Intent
import com.seucaio.unideas.core.backup.domain.repository.GoogleAuthRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSignInIntentUseCaseTest {

    private val repository: GoogleAuthRepository = mockk()
    private val useCase = GetSignInIntentUseCase(repository)

    @Test
    fun `invoke delegates to the repository`() {
        val intent: Intent = mockk()
        every { repository.getSignInIntent() } returns intent

        val result = useCase()

        assertEquals(intent, result)
        verify(exactly = 1) { repository.getSignInIntent() }
    }
}
