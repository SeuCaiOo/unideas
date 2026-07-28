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
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.seucaio.unideas.core.notifications.R
import com.seucaio.unideas.domain.model.Item

/**
 * Two channels, one notification per item plus a group summary per tier: [NORMAL_CHANNEL_ID]
 * (dismissible) and [URGENT_CHANNEL_ID] (`setOngoing(true)`, tinted with [R.color.reminder_urgent_accent]
 * for visual weight). `setOngoing` alone no longer prevents swipe-dismiss without a foreground
 * service — Android 14 lets users dismiss any ongoing notification that isn't backed by one — so
 * the urgent tier is still swipeable there; there's no non-dismissible option without taking on a
 * foreground service (rejected in the original #95 design). An item that leaves a tier (completed,
 * moved tier, or deleted) has just its own notification cancelled — the rest of the tier, and its
 * summary, are untouched as long as at least one item remains. An empty tier cancels its summary too.
 */
class ReminderNotifier(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    // Skips reposting a tier's summary whose item set didn't change since the last call —
    // refreshNow() runs on every item completion, and reposting an unchanged dismissible
    // notification re-alerts it (setOnlyAlertOnce only suppresses the alert while the notification
    // is still on screen, which isn't the case once the user has swiped it away). null means
    // "never posted yet".
    private var lastNormalIds: Set<Long>? = null
    private var lastUrgentIds: Set<Long>? = null

    init {
        createChannels()
    }

    /**
     * [silent] mutes sound/vibration/heads-up for this call's reposts (still shows/updates the
     * notification — Android has no "silently update while hidden" option) — used for
     * completion-triggered refreshes, which didn't discover anything new, as opposed to a real
     * periodic check.
     *
     * The summary's "skip if the item set didn't change" optimization only applies when [silent]
     * — that's the case the original re-alert bug was about (a completion refresh finding nothing
     * new). A non-silent call (a real periodic check, or the Settings debug "run check now"
     * button) always reposts the summary, even with an unchanged item set — otherwise the debug
     * tool would silently no-op whenever nothing changed since the last check. Individual item
     * notifications always repost (no per-item diffing) — `silent` alone already prevents any
     * alert, so skipping would only save a redundant `notify()` call, not user-visible noise.
     */
    fun notify(normal: List<Item>, urgent: List<Item>, silent: Boolean) {
        updateTier(
            items = normal,
            notificationId = NORMAL_NOTIFICATION_ID,
            channelId = NORMAL_CHANNEL_ID,
            ongoing = false,
            silent = silent,
            lastIds = { lastNormalIds },
            setLastIds = { lastNormalIds = it },
        ) { context.getString(R.string.reminder_notification_normal_title) }

        updateTier(
            items = urgent,
            notificationId = URGENT_NOTIFICATION_ID,
            channelId = URGENT_CHANNEL_ID,
            ongoing = true,
            silent = silent,
            accentColor = ContextCompat.getColor(context, R.color.reminder_urgent_accent),
            lastIds = { lastUrgentIds },
            setLastIds = { lastUrgentIds = it },
        ) { context.getString(R.string.reminder_notification_urgent_title) }
    }

    /**
     * Posts a one-off notification on the given tier's channel, ignoring real item data — debug
     * tooling (settings). Mirrors every styling flag [updateTier]'s summary post uses (icon,
     * accent color, title prefix) so this stays a faithful validation tool instead of a
     * simplified path that silently drifts from what real notifications look like.
     */
    fun notifyTest(urgent: Boolean) {
        val notificationId = if (urgent) {
            TEST_URGENT_NOTIFICATION_ID
        } else {
            TEST_NORMAL_NOTIFICATION_ID
        }
        val channelId = if (urgent) URGENT_CHANNEL_ID else NORMAL_CHANNEL_ID
        val title = context.getString(
            if (urgent) {
                R.string.reminder_notification_urgent_title
            } else {
                R.string.reminder_notification_normal_title
            }
        )
        val prefixedTitle = if (urgent) "$URGENT_TITLE_EMOJI $title" else title
        postNotification(
            notificationId = notificationId,
            channelId = channelId,
            title = prefixedTitle,
            body = context.getString(R.string.reminder_notification_test_body),
            ongoing = urgent,
            accentColor = if (urgent) {
                ContextCompat.getColor(context, R.color.reminder_urgent_accent)
            } else {
                null
            },
        )
    }

    private fun updateTier(
        items: List<Item>,
        notificationId: Int,
        channelId: String,
        ongoing: Boolean,
        silent: Boolean,
        lastIds: () -> Set<Long>?,
        setLastIds: (Set<Long>) -> Unit,
        accentColor: Int? = null,
        summaryTitle: () -> String,
    ) {
        val ids = items.map { it.id }.toSet()

        // Items that were in this tier last call but aren't anymore (completed, moved tier, or
        // deleted) — cancel just their own notification, leaving the ones still pending alone.
        val departedIds = (lastIds() ?: emptySet()) - ids
        for (departedId in departedIds) {
            notificationManager.cancel(ITEM_NOTIFICATION_ID_OFFSET + departedId.toInt())
        }

        if (items.isEmpty()) {
            notificationManager.cancel(notificationId)
            setLastIds(ids)
            return
        }

        // A tinted tier (only urgent, today) also gets an emoji title prefix — the tint alone is
        // subtle in a notification shade full of other apps' icons.
        val titlePrefix = if (accentColor != null) "$URGENT_TITLE_EMOJI " else ""

        if (!silent || ids != lastIds()) {
            val body = context.resources.getQuantityString(
                R.plurals.reminder_notification_body,
                items.size,
                items.size
            )
            postNotification(
                notificationId = notificationId,
                channelId = channelId,
                title = titlePrefix + summaryTitle(),
                body = body,
                ongoing = ongoing,
                silent = silent,
                groupKey = channelId,
                groupSummary = true,
                accentColor = accentColor,
            )
        }

        for (item in items) {
            postNotification(
                notificationId = ITEM_NOTIFICATION_ID_OFFSET + item.id.toInt(),
                channelId = channelId,
                title = titlePrefix + item.title,
                body = item.description?.let(::notificationPreview).orEmpty(),
                ongoing = ongoing,
                silent = silent,
                groupKey = channelId,
                groupSummary = false,
                itemId = item.id,
                accentColor = accentColor,
            )
        }

        setLastIds(ids)
    }

    /** Strips common inline Markdown markers and collapses whitespace — a plain-text preview, not a full renderer. */
    private fun notificationPreview(description: String): String {
        val plain = description
            .replace(MARKDOWN_MARKER_REGEX, "")
            .replace(WHITESPACE_REGEX, " ")
            .trim()
        return if (plain.length > PREVIEW_MAX_LENGTH) {
            plain.take(PREVIEW_MAX_LENGTH).trimEnd() + "…"
        } else {
            plain
        }
    }

    private fun postNotification(
        notificationId: Int,
        channelId: String,
        title: String,
        body: String,
        ongoing: Boolean,
        silent: Boolean = false,
        groupKey: String? = null,
        groupSummary: Boolean = false,
        itemId: Long? = null,
        accentColor: Int? = null,
    ) {
        if (!hasPostNotificationsPermission()) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)
            .setSilent(silent)
            .setContentIntent(contentIntent(notificationId, itemId))
            .setAutoCancel(!ongoing)
            .setGroup(groupKey)
            .setGroupSummary(groupSummary)
            .apply { accentColor?.let(::setColor) }
            .build()

        notificationManager.notify(notificationId, notification)
    }

    /**
     * [itemId] non-null (an individual item's own notification) deep-links straight to that item
     * (`unideas://item/{id}`, matching the `navDeepLink` on `ItemsRoute.Detail`) via an explicit
     * `ACTION_VIEW` + `setPackage` — explicit so it resolves directly to this app without an
     * intent chooser, same self-contained spirit as not referencing `MainActivity` by class. A
     * summary notification (no single item to open) falls back to just launching the app, via the
     * launcher intent resolved by package name.
     */
    private fun contentIntent(notificationId: Int, itemId: Long?): PendingIntent? {
        val intent = if (itemId != null) {
            Intent(Intent.ACTION_VIEW, "unideas://item/$itemId".toUri())
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

        // Offset for individual item notification IDs, kept clear of the summary/test IDs above —
        // an item can only be in one tier at a time (see ReminderTier.of), so a single offset per
        // item.id is enough, no separate ranges needed per tier.
        private const val ITEM_NOTIFICATION_ID_OFFSET = 10_000
        private const val PREVIEW_MAX_LENGTH = 120
        private const val URGENT_TITLE_EMOJI = "⚠️"
        private val MARKDOWN_MARKER_REGEX = Regex("[*_~`#]")
        private val WHITESPACE_REGEX = Regex("\\s+")

        /**
         * Long-short-long pattern (ms: wait, long, pause, short, pause, short, pause, long) designed to be distinct
         * from standard system alerts. The normal channel uses the default vibration instead.
         */
        private val URGENT_VIBRATION_PATTERN =
            longArrayOf(0, 1000, 200, 500, 800, 400, 200, 700)
    }
}
