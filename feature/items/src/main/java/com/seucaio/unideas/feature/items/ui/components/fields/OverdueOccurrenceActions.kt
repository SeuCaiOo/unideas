package com.seucaio.unideas.feature.items.ui.components.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R

@Composable
fun OverdueOccurrenceActions(
    onExtendDeadlineClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onIgnoreClicked: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        onIgnoreClicked?.let {
            OverdueActionButton(
                text = stringResource(R.string.item_detail_ignore),
                onClick = it,
                modifier = Modifier.weight(1f),
            )
        }
        OverdueActionButton(
            text = stringResource(R.string.item_detail_extend_deadline),
            onClick = onExtendDeadlineClicked,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OverdueActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
    }
}

private class OverdueOccurrenceActionsPreviewProvider : PreviewParameterProvider<Boolean> {

    override val values: Sequence<Boolean> = sequenceOf(true, false)
}

private val noopClick: () -> Unit = {}

@PreviewLightDark
@Composable
private fun OverdueOccurrenceActionsPreview(
    @PreviewParameter(OverdueOccurrenceActionsPreviewProvider::class) ignorable: Boolean,
) {
    UdsTheme {
        Surface {
            OverdueOccurrenceActions(
                onExtendDeadlineClicked = noopClick,
                onIgnoreClicked = if (ignorable) noopClick else null,
            )
        }
    }
}
