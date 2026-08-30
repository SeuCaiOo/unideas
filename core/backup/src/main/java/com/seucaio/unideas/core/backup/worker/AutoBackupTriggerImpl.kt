package com.seucaio.unideas.core.backup.worker

import android.content.Context
import com.seucaio.unideas.domain.repository.AutoBackupTrigger

class AutoBackupTriggerImpl(private val context: Context) : AutoBackupTrigger {

    override fun triggerNow() = AutoBackupScheduler.triggerNow(context)
}
