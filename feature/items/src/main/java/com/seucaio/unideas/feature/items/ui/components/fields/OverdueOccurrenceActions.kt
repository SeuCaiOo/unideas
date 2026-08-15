package com.seucaio.unideas.feature.items.ui.components.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        onIgnoreClicked?.let {
            TextButton(onClick = it, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.item_detail_ignore))
            }
        }
        TextButton(onClick = onExtendDeadlineClicked, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.item_detail_extend_deadline))
        }
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
