package com.seucaio.unideas.core.notifications.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.ColorInt
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.seucaio.unideas.core.common.extensions.hasPermission
import com.seucaio.unideas.core.notifications.R

internal data class NotificationContent(val title: String, val body: String)

internal sealed interface PostTarget {
    fun notificationId(tier: NotificationTier): Int
    fun groupKey(tier: NotificationTier): String? = tier.channelId
    val groupSummary: Boolean get() = false

    data object Summary : PostTarget {
        override fun notificationId(tier: NotificationTier) = tier.notificationId
        override val groupSummary = true
    }

    data object TestSummary : PostTarget {
        override fun notificationId(tier: NotificationTier) = tier.testNotificationId
        override fun groupKey(tier: NotificationTier): String? = null
    }

    data class PerItem(val itemId: Long) : PostTarget {
        override fun notificationId(tier: NotificationTier) =
            ITEM_NOTIFICATION_ID_OFFSET + itemId.toInt()
    }

    companion object {
        private const val ITEM_NOTIFICATION_ID_OFFSET = 10_000
    }
}

internal data class PostNotificationRequest(
    val notificationId: Int,
    val channelId: String,
    val title: String,
    val body: String,
    val ongoing: Boolean,
    val silent: Boolean = false,
    val groupKey: String? = null,
    val groupSummary: Boolean = false,
    val itemId: Long? = null,
    @param:ColorInt val accentColor: Int? = null,
) {
    companion object {
        fun forTier(
            tier: NotificationTier,
            content: NotificationContent,
            silent: Boolean,
            target: PostTarget,
            accentColor: Int?,
        ) = PostNotificationRequest(
            notificationId = target.notificationId(tier),
            channelId = tier.channelId,
            title = content.title,
            body = content.body,
            ongoing = tier.ongoing,
            silent = silent,
            groupKey = target.groupKey(tier),
            groupSummary = target.groupSummary,
            itemId = (target as? PostTarget.PerItem)?.itemId,
            accentColor = accentColor,
        )
    }
}

internal class ReminderNotificationPoster(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    fun cancel(notificationId: Int) = notificationManager.cancel(notificationId)

    fun post(request: PostNotificationRequest) {
        if (!context.hasPermission(Manifest.permission.POST_NOTIFICATIONS)) return

        val notification = NotificationCompat.Builder(context, request.channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(request.title)
            .setContentText(request.body)
            .setOngoing(request.ongoing)
            .setOnlyAlertOnce(true)
            .setSilent(request.silent)
            .setContentIntent(contentIntent(request.notificationId, request.itemId))
            .setAutoCancel(!request.ongoing)
            .setGroup(request.groupKey)
            .setGroupSummary(request.groupSummary)
            .apply { request.accentColor?.let(::setColor) }
            .build()

        notificationManager.notify(request.notificationId, notification)
    }

    private fun contentIntent(notificationId: Int, itemId: Long?): PendingIntent? {
        val intent = if (itemId != null) {
            Intent(Intent.ACTION_VIEW, "unideas://item?itemId=$itemId".toUri())
                .setPackage(context.packageName)
        } else {
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
