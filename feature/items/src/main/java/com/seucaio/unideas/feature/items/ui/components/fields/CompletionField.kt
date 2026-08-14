package com.seucaio.unideas.feature.items.ui.components.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import java.time.LocalDateTime

@Composable
fun CompletionField(
    isCompleted: Boolean,
    isLate: Boolean,
    completedAt: LocalDateTime?,
    onCompleteClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onIgnoreClicked: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onCompleteClicked,
            modifier = Modifier.weight(1f),
        ) {
            val labelRes = when {
                isCompleted -> R.string.item_detail_reopen
                isLate -> R.string.item_detail_complete_late
                else -> R.string.item_detail_complete
            }
            Text(stringResource(labelRes))
        }
        when {
            completedAt != null -> Text(
                text = stringResource(R.string.item_detail_completed_on, completedAt.toFormattedDateString()),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
            )

            isLate && onIgnoreClicked != null -> TextButton(
                onClick = onIgnoreClicked,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.item_detail_ignore))
            }
        }
    }
}

private data class CompletionFieldPreviewData(
    val isCompleted: Boolean,
    val isLate: Boolean,
    val completedAt: LocalDateTime? = null,
    val ignorable: Boolean = false,
)

private class CompletionFieldPreviewProvider : PreviewParameterProvider<CompletionFieldPreviewData> {

    override val values: Sequence<CompletionFieldPreviewData> = sequenceOf(
        CompletionFieldPreviewData(isCompleted = false, isLate = false),
        CompletionFieldPreviewData(isCompleted = false, isLate = true, ignorable = true),
        CompletionFieldPreviewData(
            isCompleted = true,
            isLate = false,
            completedAt = LocalDateTime.of(2026, 7, 20, 14, 30),
        ),
    )
}

private val noopClick: () -> Unit = {}

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
                completedAt = previewData.completedAt,
                onCompleteClicked = noopClick,
                onIgnoreClicked = if (previewData.ignorable) noopClick else null,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
