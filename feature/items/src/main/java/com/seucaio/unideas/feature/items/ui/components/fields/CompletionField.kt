package com.seucaio.unideas.feature.items.ui.components.fields

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.ds.components.lists.item.ListItemCheckbox
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import java.time.LocalDateTime

private const val CARD_TINT_ALPHA = 0.08f

@Composable
fun CompletionField(
    isCompleted: Boolean,
    isLate: Boolean,
    completedLate: Boolean,
    completedAt: LocalDateTime?,
    overdueDays: Int?,
    onCompleteClicked: () -> Unit,
    onExtendDeadlineClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onIgnoreClicked: (() -> Unit)? = null,
    remindersMuted: Boolean = false,
    onMuteRemindersToggled: (() -> Unit)? = null,
) {
    val borderColor = when {
        completedLate -> LocalUdsExtendedColors.current.warning
        isCompleted -> MaterialTheme.colorScheme.primary
        isLate -> LocalUdsExtendedColors.current.warning
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val containerColor = when {
        completedLate -> LocalUdsExtendedColors.current.warning.copy(alpha = CARD_TINT_ALPHA)
        isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = CARD_TINT_ALPHA)
        isLate -> LocalUdsExtendedColors.current.warning.copy(alpha = CARD_TINT_ALPHA)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ListItemCheckbox(
                    checked = isCompleted,
                    contentDescription = stringResource(R.string.item_detail_complete),
                    onToggle = onCompleteClicked,
                    size = 24.dp,
                    iconSize = 16.dp,
                )
                CompletionFieldLabels(
                    isCompleted = isCompleted,
                    isLate = isLate,
                    completedLate = completedLate,
                    completedAt = completedAt,
                    overdueDays = overdueDays,
                    remindersMuted = remindersMuted,
                    modifier = Modifier.weight(1f),
                )
                if (onMuteRemindersToggled != null) {
                    MuteRemindersButton(remindersMuted = remindersMuted, onClick = onMuteRemindersToggled)
                }
                if (isCompleted) {
                    TextButton(onClick = onCompleteClicked) {
                        Text(stringResource(R.string.item_detail_reopen))
                    }
                }
            }
            if (isLate) {
                OverdueOccurrenceActions(
                    onExtendDeadlineClicked = onExtendDeadlineClicked,
                    onIgnoreClicked = onIgnoreClicked,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun MuteRemindersButton(remindersMuted: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = if (remindersMuted) Icons.Outlined.NotificationsOff else Icons.Outlined.Notifications,
            contentDescription = stringResource(
                if (remindersMuted) R.string.item_detail_unmute_reminders else R.string.item_detail_mute_reminders,
            ),
        )
    }
}

@Composable
private fun CompletionFieldLabels(
    isCompleted: Boolean,
    isLate: Boolean,
    completedLate: Boolean,
    completedAt: LocalDateTime?,
    overdueDays: Int?,
    modifier: Modifier = Modifier,
    remindersMuted: Boolean = false,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(
                when {
                    completedLate -> R.string.item_detail_completed_late_status
                    isCompleted -> R.string.item_detail_completed_status
                    isLate -> R.string.item_detail_complete_late
                    else -> R.string.item_detail_complete
                }
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = when {
                completedLate -> LocalUdsExtendedColors.current.warning
                isCompleted -> LocalUdsExtendedColors.current.textTertiary
                isLate -> LocalUdsExtendedColors.current.warning
                else -> MaterialTheme.colorScheme.onSurface
            },
            textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
        )
        completedAt?.let {
            Text(
                text = stringResource(R.string.item_detail_completed_on, it.toFormattedDateString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (isLate && overdueDays != null) {
            Text(
                text = pluralStringResource(R.plurals.item_detail_overdue_days, overdueDays, overdueDays),
                style = MaterialTheme.typography.bodySmall,
                color = LocalUdsExtendedColors.current.warning,
            )
        }
        if (remindersMuted) {
            Text(
                text = stringResource(R.string.item_detail_reminders_muted_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class CompletionFieldPreviewData(
    val isCompleted: Boolean,
    val isLate: Boolean,
    val completedLate: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val overdueDays: Int? = null,
    val canIgnore: Boolean = false,
    val canMuteReminders: Boolean = false,
    val remindersMuted: Boolean = false,
)

private class CompletionFieldPreviewProvider : PreviewParameterProvider<CompletionFieldPreviewData> {

    override val values: Sequence<CompletionFieldPreviewData> = sequenceOf(
        CompletionFieldPreviewData(isCompleted = false, isLate = false),
        CompletionFieldPreviewData(isCompleted = false, isLate = false, canMuteReminders = true),
        CompletionFieldPreviewData(
            isCompleted = false,
            isLate = false,
            canMuteReminders = true,
            remindersMuted = true,
        ),
        CompletionFieldPreviewData(isCompleted = false, isLate = true, overdueDays = 2),
        CompletionFieldPreviewData(isCompleted = false, isLate = true, overdueDays = 4, canIgnore = true),
        CompletionFieldPreviewData(
            isCompleted = true,
            isLate = false,
            completedAt = LocalDateTime.of(2026, 7, 20, 14, 30),
        ),
        CompletionFieldPreviewData(
            isCompleted = true,
            isLate = false,
            completedLate = true,
            completedAt = LocalDateTime.of(2026, 7, 20, 14, 30),
        ),
    )
}

@PreviewLightDark
@Composable
private fun CompletionFieldPreview(
    @PreviewParameter(CompletionFieldPreviewProvider::class) previewData: CompletionFieldPreviewData,
) {
    UdsTheme {
        Surface {
            CompletionField(
                isCompleted = previewData.isCompleted,
                isLate = previewData.isLate,
                completedLate = previewData.completedLate,
                completedAt = previewData.completedAt,
                overdueDays = previewData.overdueDays,
                onCompleteClicked = {},
                onExtendDeadlineClicked = {},
                onIgnoreClicked = if (previewData.canIgnore) { {} } else null,
                remindersMuted = previewData.remindersMuted,
                onMuteRemindersToggled = if (previewData.canMuteReminders) { {} } else null,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
