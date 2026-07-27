package com.seucaio.unideas.core.notifications.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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

    // Skips reposting a tier whose item set didn't change since the last call — refreshNow() runs
    // on every item completion, and reposting an unchanged dismissible notification re-alerts it
    // (setOnlyAlertOnce only suppresses the alert while the notification is still on screen, which
    // isn't the case once the user has swiped it away). null means "never posted yet".
    private var lastNormalIds: Set<Long>? = null
    private var lastUrgentIds: Set<Long>? = null

    init {
        createChannels()
    }

    fun notify(normal: List<Item>, urgent: List<Item>) {
        val normalIds = normal.map { it.id }.toSet()
        if (normalIds != lastNormalIds) {
            updateTier(NORMAL_NOTIFICATION_ID, NORMAL_CHANNEL_ID, normal, ongoing = false) {
                context.getString(R.string.reminder_notification_normal_title)
            }
            lastNormalIds = normalIds
        }

        val urgentIds = urgent.map { it.id }.toSet()
        if (urgentIds != lastUrgentIds) {
            updateTier(URGENT_NOTIFICATION_ID, URGENT_CHANNEL_ID, urgent, ongoing = true) {
                context.getString(R.string.reminder_notification_urgent_title)
            }
            lastUrgentIds = urgentIds
        }
    }

    /** Posts a one-off notification on the given tier's channel, ignoring real item data — debug tooling (settings). */
    fun notifyTest(urgent: Boolean) {
        val notificationId = if (urgent) TEST_URGENT_NOTIFICATION_ID else TEST_NORMAL_NOTIFICATION_ID
        val channelId = if (urgent) URGENT_CHANNEL_ID else NORMAL_CHANNEL_ID
        val title = context.getString(
            if (urgent) R.string.reminder_notification_urgent_title else R.string.reminder_notification_normal_title
        )
        postNotification(
            notificationId = notificationId,
            channelId = channelId,
            title = title,
            body = context.getString(R.string.reminder_notification_test_body),
            ongoing = urgent,
        )
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

        val body = context.resources.getQuantityString(
            R.plurals.reminder_notification_body,
            items.size,
            items.size
        )
        postNotification(notificationId, channelId, title(), body, ongoing)
    }

    private fun postNotification(notificationId: Int, channelId: String, title: String, body: String, ongoing: Boolean) {
        if (!hasPostNotificationsPermission()) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent(notificationId))
            .setAutoCancel(!ongoing)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    /**
     * Launches the app's own launcher activity by package name rather than referencing it by
     * class — this module stays self-contained (no dependency on `:app`), same as
     * [com.seucaio.unideas.core.notifications.di.NotificationsModule]'s pattern.
     */
    private fun contentIntent(notificationId: Int): PendingIntent? {
        val launchIntent =
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
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
        ).apply {
            description = context.getString(R.string.reminder_channel_urgent_description)
            enableVibration(true)
            vibrationPattern = URGENT_VIBRATION_PATTERN
        }

        notificationManager.createNotificationChannels(listOf(normal, urgent))
    }

    companion object {
        const val NORMAL_CHANNEL_ID = "reminder_normal"
        const val URGENT_CHANNEL_ID = "reminder_urgent"
        private const val NORMAL_NOTIFICATION_ID = 1001
        private const val URGENT_NOTIFICATION_ID = 1002
        private const val TEST_NORMAL_NOTIFICATION_ID = 1003
        private const val TEST_URGENT_NOTIFICATION_ID = 1004

        /**
         * Long-short-long pattern (ms: wait, long, pause, short, pause, short, pause, long) designed to be distinct
         * from standard system alerts. The normal channel uses the default vibration instead.
         */
        private val URGENT_VIBRATION_PATTERN =
            longArrayOf(0, 1000, 200, 500, 800, 400, 200, 700)
    }
}
