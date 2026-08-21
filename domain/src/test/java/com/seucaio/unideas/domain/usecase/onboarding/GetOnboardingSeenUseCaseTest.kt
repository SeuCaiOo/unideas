package com.seucaio.unideas.domain.usecase.onboarding

import com.seucaio.unideas.domain.repository.OnboardingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetOnboardingSeenUseCaseTest {

    private val repository: OnboardingRepository = mockk()
    private val useCase = GetOnboardingSeenUseCase(repository)

    @Test
    fun `invoke returns the flag from the repository`() = runTest {
        coEvery { repository.isOnboardingSeen() } returns true

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        coVerify(exactly = 1) { repository.isOnboardingSeen() }
    }

    @Test
    fun `invoke fails when the repository throws`() = runTest {
        coEvery { repository.isOnboardingSeen() } throws IllegalStateException("boom")

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
