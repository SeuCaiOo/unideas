package com.seucaio.unideas.core.backup.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.Drive
import com.seucaio.unideas.core.backup.domain.repository.GoogleAuthRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildDriveServiceUseCaseTest {

    private val repository: GoogleAuthRepository = mockk()
    private val useCase = BuildDriveServiceUseCase(repository)

    @Test
    fun `invoke delegates the account to the repository`() {
        val account: GoogleSignInAccount = mockk()
        val drive: Drive = mockk()
        every { repository.buildDriveService(account) } returns drive

        val result = useCase(account)

        assertEquals(drive, result)
        verify(exactly = 1) { repository.buildDriveService(account) }
    }
}
