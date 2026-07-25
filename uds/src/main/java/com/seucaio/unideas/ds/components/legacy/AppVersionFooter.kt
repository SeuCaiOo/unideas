package com.seucaio.unideas.ds.components.legacy

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.ds.theme.UdsTheme

@Composable
fun AppVersionFooter(versionName: String, modifier: Modifier = Modifier) {
    Text(
        text = "v$versionName",
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

@PreviewLightDark
@Composable
private fun AppVersionFooterPreview() {
    UdsTheme {
        Surface {
            AppVersionFooter(versionName = "0.0.2")
        }
    }
}
