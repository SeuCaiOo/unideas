package com.seucaio.unideas.core.backup.worker

import androidx.room.InvalidationTracker
import com.seucaio.unideas.data.local.database.UnideasDatabase
import com.seucaio.unideas.domain.repository.AutoBackupTrigger
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class AutoBackupDataObserverTest {

    @MockK
    private lateinit var database: UnideasDatabase

    @MockK(relaxed = true)
    private lateinit var invalidationTracker: InvalidationTracker

    @MockK
    private lateinit var autoBackupTrigger: AutoBackupTrigger

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { database.invalidationTracker } returns invalidationTracker
    }

    @Test
    fun `when constructed should register an observer on the database`() {
        AutoBackupDataObserver(database, autoBackupTrigger)

        verify(exactly = 1) { invalidationTracker.addObserver(any()) }
    }

    @Test
    fun `when the registered observer is invalidated should trigger auto-backup`() {
        val observerSlot = slot<InvalidationTracker.Observer>()
        every { invalidationTracker.addObserver(capture(observerSlot)) } returns Unit
        every { autoBackupTrigger.triggerNow() } returns Unit

        AutoBackupDataObserver(database, autoBackupTrigger)
        observerSlot.captured.onInvalidated(setOf("items"))

        verify(exactly = 1) { autoBackupTrigger.triggerNow() }
    }
}
