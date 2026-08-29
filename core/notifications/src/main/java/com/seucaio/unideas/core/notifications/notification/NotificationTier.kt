package com.seucaio.unideas.core.notifications.notification

import android.app.NotificationManager
import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.seucaio.unideas.core.notifications.R

private const val NORMAL_NOTIFICATION_ID = 1001
private const val URGENT_NOTIFICATION_ID = 1002
private const val TEST_NORMAL_NOTIFICATION_ID = 1003
private const val TEST_URGENT_NOTIFICATION_ID = 1004

/**
 * Long-short-long pattern (ms: wait, long, pause, short, pause, short, pause, long) designed to be distinct
 * from standard system alerts. The normal channel uses the default vibration instead.
 */
private val URGENT_VIBRATION_PATTERN = longArrayOf(0, 1000, 200, 500, 800, 400, 200, 700)

internal data class ChannelConfig(
    val id: String,
    @param:StringRes val nameRes: Int,
    @param:StringRes val descriptionRes: Int,
    val importance: Int,
    val vibrationPattern: LongArray? = null,
)

internal data class NotificationIds(val summaryId: Int, val testId: Int)

internal data class NotificationConfig(
    val ids: NotificationIds,
    @param:StringRes val titleRes: Int,
    val ongoing: Boolean,
    val hasEmoji: Boolean = false,
    @param:ColorRes val accentColorRes: Int? = null,
)

internal enum class NotificationTier(
    val channel: ChannelConfig,
    val notification: NotificationConfig,
) {
    NORMAL(
        channel = ChannelConfig(
            id = "reminder_normal",
            nameRes = R.string.reminder_channel_normal_name,
            descriptionRes = R.string.reminder_channel_normal_description,
            importance = NotificationManager.IMPORTANCE_DEFAULT,
        ),
        notification = NotificationConfig(
            ids = NotificationIds(
                summaryId = NORMAL_NOTIFICATION_ID,
                testId = TEST_NORMAL_NOTIFICATION_ID
            ),
            titleRes = R.string.reminder_notification_normal_title,
            ongoing = false,
        ),
    ),

    URGENT(
        channel = ChannelConfig(
            id = "reminder_urgent",
            nameRes = R.string.reminder_channel_urgent_name,
            descriptionRes = R.string.reminder_channel_urgent_description,
            importance = NotificationManager.IMPORTANCE_HIGH,
            vibrationPattern = URGENT_VIBRATION_PATTERN,
        ),
        notification = NotificationConfig(
            ids = NotificationIds(
                summaryId = URGENT_NOTIFICATION_ID,
                testId = TEST_URGENT_NOTIFICATION_ID
            ),
            titleRes = R.string.reminder_notification_urgent_title,
            ongoing = true,
            hasEmoji = true,
            accentColorRes = R.color.reminder_urgent_accent,
        ),
    );

    val channelId: String get() = channel.id
    val notificationId: Int get() = notification.ids.summaryId
    val testNotificationId: Int get() = notification.ids.testId
    val titleRes: Int get() = notification.titleRes
    val ongoing: Boolean get() = notification.ongoing
    val hasEmoji: Boolean get() = notification.hasEmoji

    @ColorInt
    fun getAccentColor(context: Context): Int? =
        notification.accentColorRes?.let { ContextCompat.getColor(context, it) }
}
