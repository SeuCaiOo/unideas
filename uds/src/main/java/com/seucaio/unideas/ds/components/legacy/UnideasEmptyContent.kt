package com.seucaio.unideas.ds.components.legacy

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * Exception to `:uds`'s "no `R.*` references" portability rule (see module README) —
 * `legacy/` only exists to receive `:core:ui` components verbatim while `:core:ui` is
 * being emptied out, and gets deleted once that's done, so it doesn't need to hold to
 * the portable-module contract the rest of `:uds` does.
 */
@Composable
fun UnideasEmptyContent(
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewLightDark
@Composable
private fun UnideasEmptyContentPreview() {
    UdsTheme {
        Surface {
            UnideasEmptyContent(messageRes = android.R.string.ok)
        }
    }
}
