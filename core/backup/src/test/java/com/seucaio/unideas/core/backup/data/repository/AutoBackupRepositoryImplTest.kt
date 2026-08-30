package com.seucaio.unideas.core.backup.data.repository

import com.seucaio.unideas.core.backup.data.local.datastore.AutoBackupPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutoBackupRepositoryImplTest {

    private val preferences: AutoBackupPreferences = mockk()
    private val repository = AutoBackupRepositoryImpl(preferences)

    @Test
    fun `isEnabled delegates to preferences`() = runTest {
        coEvery { preferences.isEnabled() } returns true

        val result = repository.isEnabled()

        assertEquals(true, result)
        coVerify(exactly = 1) { preferences.isEnabled() }
    }

    @Test
    fun `setEnabled delegates to preferences`() = runTest {
        coEvery { preferences.setEnabled(true) } returns Unit

        repository.setEnabled(true)

        coVerify(exactly = 1) { preferences.setEnabled(true) }
    }

    @Test
    fun `getTrackedFileId delegates to preferences`() = runTest {
        coEvery { preferences.getTrackedFileId() } returns null

        val result = repository.getTrackedFileId()

        assertNull(result)
        coVerify(exactly = 1) { preferences.getTrackedFileId() }
    }

    @Test
    fun `setTrackedFileId delegates to preferences`() = runTest {
        coEvery { preferences.setTrackedFileId("file-1") } returns Unit

        repository.setTrackedFileId("file-1")

        coVerify(exactly = 1) { preferences.setTrackedFileId("file-1") }
    }
}
