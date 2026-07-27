package com.seucaio.unideas.core.notifications.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.seucaio.unideas.core.notifications.R
import com.seucaio.unideas.domain.model.Item

/**
 * Two channels, one aggregated notification per tier (never per item): [NORMAL_CHANNEL_ID]
 * (dismissible) and [URGENT_CHANNEL_ID] (`setOngoing(true)` — no foreground service needed for
 * that to stick; the Android 13 swipe-away change only applies to foreground-service
 * notifications). An empty tier cancels its notification instead of showing an empty one.
 */
class ReminderNotifier(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createChannels()
    }

    fun notify(normal: List<Item>, urgent: List<Item>) {
        updateTier(NORMAL_NOTIFICATION_ID, NORMAL_CHANNEL_ID, normal, ongoing = false) {
            context.getString(R.string.reminder_notification_normal_title)
        }
        updateTier(URGENT_NOTIFICATION_ID, URGENT_CHANNEL_ID, urgent, ongoing = true) {
            context.getString(R.string.reminder_notification_urgent_title)
        }
    }

    private fun updateTier(
        notificationId: Int,
        channelId: String,
        items: List<Item>,
        ongoing: Boolean,
        title: () -> String,
    ) {
        if (items.isEmpty()) {
            notificationManager.cancel(notificationId)
            return
        }
        if (!hasPostNotificationsPermission()) return

        val body = context.resources.getQuantityString(R.plurals.reminder_notification_body, items.size, items.size)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title())
            .setContentText(body)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun hasPostNotificationsPermission(): Boolean =
        ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun createChannels() {
        val normal = NotificationChannel(
            NORMAL_CHANNEL_ID,
            context.getString(R.string.reminder_channel_normal_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.reminder_channel_normal_description) }

        val urgent = NotificationChannel(
            URGENT_CHANNEL_ID,
            context.getString(R.string.reminder_channel_urgent_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = context.getString(R.string.reminder_channel_urgent_description) }

        notificationManager.createNotificationChannels(listOf(normal, urgent))
    }

    companion object {
        const val NORMAL_CHANNEL_ID = "reminder_normal"
        const val URGENT_CHANNEL_ID = "reminder_urgent"
        private const val NORMAL_NOTIFICATION_ID = 1001
        private const val URGENT_NOTIFICATION_ID = 1002
    }
}
