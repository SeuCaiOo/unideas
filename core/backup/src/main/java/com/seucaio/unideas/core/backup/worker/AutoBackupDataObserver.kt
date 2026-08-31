package com.seucaio.unideas.core.backup.worker

import androidx.room.InvalidationTracker
import com.seucaio.unideas.data.local.database.UnideasDatabase
import com.seucaio.unideas.domain.repository.AutoBackupTrigger

class AutoBackupDataObserver(
    private val database: UnideasDatabase,
    private val autoBackupTrigger: AutoBackupTrigger,
) {

    private val observer = object : InvalidationTracker.Observer(TRACKED_TABLES) {
        override fun onInvalidated(tables: Set<String>) = autoBackupTrigger.triggerNow()
    }

    init {
        database.invalidationTracker.addObserver(observer)
    }

    private companion object {
        val TRACKED_TABLES =
            arrayOf("items", "sections", "tags", "item_tag", "item_completion_history")
    }
}
