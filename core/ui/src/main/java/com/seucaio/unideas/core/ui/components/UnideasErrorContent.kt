package com.seucaio.unideas.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.core.ui.theme.UnideasTheme

/** Uses the M3 `error`/`errorContainer` role — a separate semantic from due-date urgency colors. */
@Composable
fun UnideasErrorContent(
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        if (onRetry != null) {
            Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
                Text(stringResource(android.R.string.ok))
            }
        }
    }
}

@Preview
@Composable
private fun UnideasErrorContentPreview() {
    UnideasTheme {
        UnideasErrorContent(messageRes = android.R.string.ok)
    }
}

@Preview
@Composable
private fun UnideasErrorContentWithRetryPreview() {
    UnideasTheme {
        UnideasErrorContent(messageRes = android.R.string.ok, onRetry = {})
    }
}
