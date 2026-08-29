package com.seucaio.unideas.core.backup.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AutoBackupSettingsUseCaseTest {

    private val getAutoBackupEnabledUseCase: GetAutoBackupEnabledUseCase = mockk()
    private val setAutoBackupEnabledUseCase: SetAutoBackupEnabledUseCase = mockk()
    private val useCase = AutoBackupSettingsUseCase(getAutoBackupEnabledUseCase, setAutoBackupEnabledUseCase)

    @Test
    fun `isEnabled delegates to GetAutoBackupEnabledUseCase`() = runTest {
        coEvery { getAutoBackupEnabledUseCase() } returns true

        val result = useCase.isEnabled()

        assertEquals(true, result)
        coVerify(exactly = 1) { getAutoBackupEnabledUseCase() }
    }

    @Test
    fun `setEnabled delegates to SetAutoBackupEnabledUseCase`() = runTest {
        coEvery { setAutoBackupEnabledUseCase(true) } returns Unit

        useCase.setEnabled(true)

        coVerify(exactly = 1) { setAutoBackupEnabledUseCase(true) }
    }
}
