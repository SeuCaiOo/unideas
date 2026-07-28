package com.seucaio.unideas.core.notifications.worker

import android.content.Context
import com.seucaio.unideas.domain.repository.ReminderRefreshTrigger

class ReminderRefreshTriggerImpl(private val context: Context) : ReminderRefreshTrigger {

    override fun refreshNow() = ReminderScheduler.refreshNow(context, silent = true)
}
