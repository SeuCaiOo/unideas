package com.seucaio.unideas.core.notifications.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.seucaio.unideas.core.common.extensions.stripMarkdownPreview
import com.seucaio.unideas.core.notifications.BuildConfig
import com.seucaio.unideas.core.notifications.R
import com.seucaio.unideas.domain.model.Item

class ReminderNotifier(private val context: Context) {

    private val poster = ReminderNotificationPoster(context)

    private val normalTierState = TierState()
    private val urgentTierState = TierState()

    // Debug and release builds have distinct applicationIds and thus show as separate apps in the
    // notification shade — with identical titles, there's no visual way to tell which build a
    // notification came from while both are installed on the same device.
    private val debugTitlePrefix = if (BuildConfig.DEBUG) "[DEBUG] " else ""

    init {
        createReminderNotificationChannels(context, NotificationManagerCompat.from(context))
    }

    fun notify(normal: List<Item>, urgent: List<Item>, silent: Boolean) {
        updateTier(NotificationTier.NORMAL, normal, normalTierState, silent)
        updateTier(NotificationTier.URGENT, urgent, urgentTierState, silent)
    }

    fun notifyTest(urgent: Boolean) {
        val tier = if (urgent) NotificationTier.URGENT else NotificationTier.NORMAL
        poster.post(
            PostNotificationRequest.forTier(
                tier = tier,
                content = NotificationContent(
                    title = buildTitle(context.getString(tier.titleRes), tier),
                    body = context.getString(R.string.reminder_notification_test_body),
                ),
                silent = false,
                target = PostTarget.TestSummary,
                accentColor = tier.getAccentColor(context),
            )
        )
    }

    private fun updateTier(
        tier: NotificationTier,
        items: List<Item>,
        state: TierState,
        silent: Boolean
    ) {
        val ids = items.map { it.id }.toSet()
        cancelDepartedItems(state.lastIds, ids, tier)

        if (items.isEmpty()) {
            poster.cancel(tier.notificationId)
            state.lastIds = ids
            return
        }

        val accentColor = tier.getAccentColor(context)
        if (!silent || ids != state.lastIds) {
            poster.post(
                PostNotificationRequest.forTier(
                    tier = tier,
                    content = NotificationContent(
                        title = buildTitle(context.getString(tier.titleRes), tier),
                        body = context.resources.getQuantityString(
                            R.plurals.reminder_notification_body,
                            items.size,
                            items.size
                        ),
                    ),
                    silent = silent,
                    target = PostTarget.Summary,
                    accentColor = accentColor,
                )
            )
        }
        items.forEach { item ->
            poster.post(
                PostNotificationRequest.forTier(
                    tier = tier,
                    content = NotificationContent(
                        title = buildTitle(item.title, tier),
                        body = item.description?.stripMarkdownPreview(PREVIEW_MAX_LENGTH).orEmpty(),
                    ),
                    silent = silent,
                    target = PostTarget.PerItem(item.id),
                    accentColor = accentColor,
                )
            )
        }

        state.lastIds = ids
    }

    // Items that were in this tier last call but aren't anymore (completed, moved tier, or
    // deleted) — cancel just their own notification, leaving the ones still pending alone.
    private fun cancelDepartedItems(lastIds: Set<Long>?, currentIds: Set<Long>, tier: NotificationTier) {
        val departedIds = (lastIds ?: emptySet()) - currentIds
        for (departedId in departedIds) {
            poster.cancel(PostTarget.PerItem(departedId).notificationId(tier))
        }
    }

    private fun buildTitle(base: String, tier: NotificationTier): String {
        val prefix = if (tier.hasEmoji) "$URGENT_TITLE_EMOJI " else ""
        return "$debugTitlePrefix$prefix$base"
    }

    private class TierState {
        var lastIds: Set<Long>? = null
    }

    companion object {
        private const val PREVIEW_MAX_LENGTH = 120
        private const val URGENT_TITLE_EMOJI = "⚠️"
    }
}
