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
    private val getAutoBackupTrackedFileIdUseCase: GetAutoBackupTrackedFileIdUseCase = mockk()
    private val setAutoBackupTrackedFileIdUseCase: SetAutoBackupTrackedFileIdUseCase = mockk()
    private val useCase = AutoBackupSettingsUseCase(
        getAutoBackupEnabledUseCase,
        setAutoBackupEnabledUseCase,
        getAutoBackupTrackedFileIdUseCase,
        setAutoBackupTrackedFileIdUseCase,
    )

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

    @Test
    fun `getTrackedFileId delegates to GetAutoBackupTrackedFileIdUseCase`() = runTest {
        coEvery { getAutoBackupTrackedFileIdUseCase() } returns "file-1"

        val result = useCase.getTrackedFileId()

        assertEquals("file-1", result)
        coVerify(exactly = 1) { getAutoBackupTrackedFileIdUseCase() }
    }

    @Test
    fun `setTrackedFileId delegates to SetAutoBackupTrackedFileIdUseCase`() = runTest {
        coEvery { setAutoBackupTrackedFileIdUseCase("file-1") } returns Unit

        useCase.setTrackedFileId("file-1")

        coVerify(exactly = 1) { setAutoBackupTrackedFileIdUseCase("file-1") }
    }
}
