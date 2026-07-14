package com.seucaio.unideas.ds.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.AppType
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.UdsTheme

@Composable
fun GroupHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = AppType.FieldLabel,
        color = LocalUdsExtendedColors.current.textTertiary,
        modifier = modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp)
    )
}

@PreviewLightDark
@Composable
private fun GroupHeaderPreview() {
    UdsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            GroupHeader("Account")
        }
    }
}
