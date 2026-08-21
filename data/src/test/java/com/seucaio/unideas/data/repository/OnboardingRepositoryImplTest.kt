package com.seucaio.unideas.data.repository

import com.seucaio.unideas.data.local.datastore.OnboardingPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingRepositoryImplTest {

    private val preferences: OnboardingPreferences = mockk()
    private val repository = OnboardingRepositoryImpl(preferences)

    @Test
    fun `isOnboardingSeen delegates to preferences`() = runTest {
        coEvery { preferences.isSeen() } returns true

        val result = repository.isOnboardingSeen()

        assertTrue(result)
        coVerify(exactly = 1) { preferences.isSeen() }
    }

    @Test
    fun `setOnboardingSeen delegates to preferences`() = runTest {
        coEvery { preferences.setSeen(true) } returns Unit

        repository.setOnboardingSeen(true)

        coVerify(exactly = 1) { preferences.setSeen(true) }
    }
}
