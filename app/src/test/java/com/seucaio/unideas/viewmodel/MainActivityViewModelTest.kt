package com.seucaio.unideas.viewmodel

import com.seucaio.unideas.domain.usecase.onboarding.GetOnboardingSeenUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
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
class MainActivityViewModelTest {

    @MockK
    private lateinit var getOnboardingSeen: GetOnboardingSeenUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when onboarding already seen should resolve needsOnboarding to false`() = runTest {
        coEvery { getOnboardingSeen() } returns Result.success(true)

        val vm = MainActivityViewModel(getOnboardingSeen)

        assertEquals(false, vm.needsOnboarding.value)
    }

    @Test
    fun `when onboarding not yet seen should resolve needsOnboarding to true`() = runTest {
        coEvery { getOnboardingSeen() } returns Result.success(false)

        val vm = MainActivityViewModel(getOnboardingSeen)

        assertEquals(true, vm.needsOnboarding.value)
    }

    @Test
    fun `when the use case fails should default to needing onboarding`() = runTest {
        coEvery { getOnboardingSeen() } returns Result.failure(IllegalStateException("boom"))

        val vm = MainActivityViewModel(getOnboardingSeen)

        assertEquals(true, vm.needsOnboarding.value)
    }
}
