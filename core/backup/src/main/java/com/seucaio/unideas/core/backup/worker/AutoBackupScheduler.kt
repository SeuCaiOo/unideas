package com.seucaio.unideas.core.backup.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object AutoBackupScheduler {

    private const val WORK_NAME = "auto_backup"

    fun triggerNow(context: Context) {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = OneTimeWorkRequestBuilder<AutoBackupWorker>().setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }
}
