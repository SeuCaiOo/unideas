package com.seucaio.unideas.core.backup.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/** Enqueues [AutoBackupWorker], deduplicating against any run still pending/in-flight. */
object AutoBackupScheduler {

    private const val WORK_NAME = "auto_backup"

    fun triggerNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<AutoBackupWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }
}
