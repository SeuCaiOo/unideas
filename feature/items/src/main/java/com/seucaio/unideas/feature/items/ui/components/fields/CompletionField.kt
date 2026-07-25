package com.seucaio.unideas.feature.items.ui.components.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import java.time.LocalDateTime

@Composable
fun CompletionField(
    isCompleted: Boolean,
    completedAt: LocalDateTime?,
    onCompleteClicked: () -> Unit,
    modifier: Modifier = Modifier,
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
            Text(stringResource(if (isCompleted) R.string.item_detail_reopen else R.string.item_detail_complete))
        }
        completedAt?.let {
            Text(
                text = stringResource(R.string.item_detail_completed_on, it.toFormattedDateString()),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun CompletionFieldNotDonePreview() {
    UdsTheme {
        Surface {
            CompletionField(
                isCompleted = false,
                completedAt = null,
                onCompleteClicked = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun CompletionFieldDonePreview() {
    UdsTheme {
        Surface {
            CompletionField(
                isCompleted = true,
                completedAt = LocalDateTime.of(2026, 7, 20, 14, 30),
                onCompleteClicked = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
