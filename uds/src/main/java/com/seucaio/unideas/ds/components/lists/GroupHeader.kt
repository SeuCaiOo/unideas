package com.seucaio.unideas.ds.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.AppType
import com.seucaio.unideas.ds.theme.Background
import com.seucaio.unideas.ds.theme.DsTheme
import com.seucaio.unideas.ds.theme.TextTertiary

@Composable
fun GroupHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = AppType.FieldLabel,
        color = TextTertiary,
        modifier = modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp)
    )
}

@PreviewLightDark
@Composable
private fun GroupHeaderPreview() {
    DsTheme {
        Box(Modifier.background(Background)) {
            GroupHeader("Account")
        }
    }
}
