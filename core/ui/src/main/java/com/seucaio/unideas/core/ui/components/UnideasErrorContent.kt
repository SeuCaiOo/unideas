package com.seucaio.unideas.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.seucaio.unideas.core.ui.R
import com.seucaio.unideas.core.ui.theme.UnideasTheme

/**
 * Full-screen error state — title is generic and owned by this component; [messageRes]
 * is the caller's specific failure description. No back button here — the screen's own
 * TopBar already has one. No FAB either: an error/loading screen has no data yet, so
 * there's nothing else actionable on it besides retrying.
 * Uses the M3 `error`/`errorContainer` role — a separate semantic from due-date urgency colors.
 */
@Composable
fun UnideasErrorContent(
    @StringRes messageRes: Int,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.error_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(onClick = onRetry, modifier = Modifier.padding(top = 24.dp)) {
            Text(stringResource(R.string.error_action_retry))
        }
    }
}

@PreviewLightDark
@Composable
private fun UnideasErrorContentPreview() {
    UnideasTheme {
        Surface {
            UnideasErrorContent(messageRes = android.R.string.ok, onRetry = {})
        }
    }
}
