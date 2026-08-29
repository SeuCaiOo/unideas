package com.seucaio.unideas.core.notifications.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.ColorInt
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.seucaio.unideas.core.common.extensions.hasPermission
import com.seucaio.unideas.core.common.extensions.stripMarkdownPreview
import com.seucaio.unideas.core.notifications.BuildConfig
import com.seucaio.unideas.core.notifications.R
import com.seucaio.unideas.domain.model.Item

/**
 * Two channels, one notification per item plus a group summary per tier — see [NotificationTier]
 * for per-tier channel/styling config. `setOngoing` alone no longer prevents swipe-dismiss without
 * a foreground service — Android 14 lets users dismiss any ongoing notification that isn't backed
 * by one — so the urgent tier is still swipeable there; there's no non-dismissible option without
 * taking on a foreground service (rejected in the original #95 design). An item that leaves a tier
 * (completed, moved tier, or deleted) has just its own notification cancelled — the rest of the
 * tier, and its summary, are untouched as long as at least one item remains. An empty tier cancels
 * its summary too.
 */
class ReminderNotifier(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    private val normalTierState = TierState()
    private val urgentTierState = TierState()

    // Debug and release builds have distinct applicationIds and thus show as separate apps in the
    // notification shade — with identical titles, there's no visual way to tell which build a
    // notification came from while both are installed on the same device.
    private val debugTitlePrefix = if (BuildConfig.DEBUG) "[DEBUG] " else ""

    init {
        createReminderNotificationChannels(context, notificationManager)
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
        updateTier(NotificationTier.NORMAL, normal, normalTierState, silent)
        updateTier(NotificationTier.URGENT, urgent, urgentTierState, silent)
    }

    /**
     * Posts a one-off notification on the given tier's channel, ignoring real item data — debug
     * tooling (settings). Mirrors every styling flag [updateTier]'s summary post uses (icon,
     * accent color, title prefix) so this stays a faithful validation tool instead of a
     * simplified path that silently drifts from what real notifications look like.
     */
    fun notifyTest(urgent: Boolean) {
        val tier = if (urgent) NotificationTier.URGENT else NotificationTier.NORMAL
        postNotification(
            notificationId = tier.testNotificationId,
            channelId = tier.channelId,
            title = buildTitle(context.getString(tier.titleRes), tier),
            body = context.getString(R.string.reminder_notification_test_body),
            ongoing = tier.ongoing,
            accentColor = tier.getAccentColor(context),
        )
    }

    private fun updateTier(tier: NotificationTier, items: List<Item>, state: TierState, silent: Boolean) {
        val ids = items.map { it.id }.toSet()

        // Items that were in this tier last call but aren't anymore (completed, moved tier, or
        // deleted) — cancel just their own notification, leaving the ones still pending alone.
        val departedIds = (state.lastIds ?: emptySet()) - ids
        for (departedId in departedIds) {
            notificationManager.cancel(ITEM_NOTIFICATION_ID_OFFSET + departedId.toInt())
        }

        if (items.isEmpty()) {
            notificationManager.cancel(tier.notificationId)
            state.lastIds = ids
            return
        }

        val accentColor = tier.getAccentColor(context)

        if (!silent || ids != state.lastIds) {
            val body = context.resources.getQuantityString(
                R.plurals.reminder_notification_body,
                items.size,
                items.size
            )
            postNotification(
                notificationId = tier.notificationId,
                channelId = tier.channelId,
                title = buildTitle(context.getString(tier.titleRes), tier),
                body = body,
                ongoing = tier.ongoing,
                silent = silent,
                groupKey = tier.channelId,
                groupSummary = true,
                accentColor = accentColor,
            )
        }

        for (item in items) {
            postNotification(
                notificationId = ITEM_NOTIFICATION_ID_OFFSET + item.id.toInt(),
                channelId = tier.channelId,
                title = buildTitle(item.title, tier),
                body = item.description?.stripMarkdownPreview(PREVIEW_MAX_LENGTH).orEmpty(),
                ongoing = tier.ongoing,
                silent = silent,
                groupKey = tier.channelId,
                groupSummary = false,
                itemId = item.id,
                accentColor = accentColor,
            )
        }

        state.lastIds = ids
    }

    private fun buildTitle(base: String, tier: NotificationTier): String {
        val prefix = if (tier.hasEmoji) "$URGENT_TITLE_EMOJI " else ""
        return "$debugTitlePrefix$prefix$base"
    }

    @SuppressLint("MissingPermission")
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
        @ColorInt accentColor: Int? = null,
    ) {
        if (!context.hasPermission(Manifest.permission.POST_NOTIFICATIONS)) return

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
     * (`unideas://item?itemId={id}`) via an explicit `ACTION_VIEW` + `setPackage` — explicit so it
     * resolves directly to this app without an intent chooser, same self-contained spirit as not
     * referencing `MainActivity` by class. `itemId` has to be a query param, not a path segment:
     * `ItemsRoute.Detail`'s type-safe `navDeepLink` generates its URI pattern from the route class,
     * and a field with a default value (both `itemId` and `initialType` have one) becomes a query
     * param in that pattern, not a path segment — a path-shaped URI silently fails to match any
     * destination instead of erroring, falling back to whatever's already on the back stack. A
     * summary notification (no single item to open) falls back to just launching the app, via the
     * launcher intent resolved by package name.
     */
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

    // Skips reposting a tier's summary whose item set didn't change since the last call —
    // refreshNow() runs on every item completion, and reposting an unchanged dismissible
    // notification re-alerts it (setOnlyAlertOnce only suppresses the alert while the notification
    // is still on screen, which isn't the case once the user has swiped it away). null means
    // "never posted yet".
    private class TierState {
        var lastIds: Set<Long>? = null
    }

    companion object {
        // Offset for individual item notification IDs, kept clear of the summary/test IDs above —
        // an item is only ever in one tier at a time, so a single offset per item.id is enough, no
        // separate ranges needed per tier. Safe to truncate item.id (Long) via toInt(): local task
        // IDs won't approach Int.MAX_VALUE.
        private const val ITEM_NOTIFICATION_ID_OFFSET = 10_000
        private const val PREVIEW_MAX_LENGTH = 120
        private const val URGENT_TITLE_EMOJI = "⚠️"
    }
}
