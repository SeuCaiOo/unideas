package com.seucaio.unideas.core.backup.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.seucaio.unideas.core.backup.domain.usecase.PerformAutoBackupUseCase

/** Runs an out-of-cycle automatic backup on-demand (via [AutoBackupScheduler.triggerNow]). */
class AutoBackupWorker(
    context: Context,
    params: WorkerParameters,
    private val performAutoBackup: PerformAutoBackupUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        performAutoBackup()
        return Result.success()
    }
}
