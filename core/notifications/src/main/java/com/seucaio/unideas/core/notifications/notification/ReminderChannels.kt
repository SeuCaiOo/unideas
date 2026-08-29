package com.seucaio.unideas.core.notifications.notification

import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

internal fun createReminderNotificationChannels(
    context: Context,
    notificationManager: NotificationManagerCompat
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val channels = NotificationTier.entries.map { tier ->
        val config = tier.channel
        NotificationChannel(
            config.id,
            context.getString(config.nameRes),
            config.importance,
        ).apply {
            description = context.getString(config.descriptionRes)
            config.vibrationPattern?.let {
                enableVibration(true)
                vibrationPattern = it
            }
        }
    }

    notificationManager.createNotificationChannels(channels)
}
