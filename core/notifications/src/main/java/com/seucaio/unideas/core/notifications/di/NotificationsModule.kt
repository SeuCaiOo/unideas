package com.seucaio.unideas.core.notifications.di

import com.seucaio.unideas.core.notifications.notification.ReminderNotifier
import com.seucaio.unideas.core.notifications.worker.ReminderCheckWorker
import com.seucaio.unideas.core.notifications.worker.ReminderRefreshTriggerImpl
import com.seucaio.unideas.domain.repository.ReminderRefreshTrigger
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Koin module for `:core:notifications` — DI is local to the module, per project convention. */
val notificationsModule = module {
    single { ReminderNotifier(androidContext()) }
    single { ReminderRefreshTriggerImpl(androidContext()) }.bind<ReminderRefreshTrigger>()
    workerOf(::ReminderCheckWorker)
}
