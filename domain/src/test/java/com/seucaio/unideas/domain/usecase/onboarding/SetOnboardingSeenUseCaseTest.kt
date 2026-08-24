package com.seucaio.unideas.domain.usecase.onboarding

import com.seucaio.unideas.domain.repository.OnboardingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SetOnboardingSeenUseCaseTest {

    private val repository: OnboardingRepository = mockk()
    private val useCase = SetOnboardingSeenUseCase(repository)

    @Test
    fun `invoke delegates to the repository`() = runTest {
        coEvery { repository.setOnboardingSeen(true) } returns Unit

        val result = useCase(true)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.setOnboardingSeen(true) }
    }

    @Test
    fun `invoke fails when the repository throws`() = runTest {
        coEvery { repository.setOnboardingSeen(false) } throws IllegalStateException("boom")

        val result = useCase(false)

        assertTrue(result.isFailure)
    }
}
