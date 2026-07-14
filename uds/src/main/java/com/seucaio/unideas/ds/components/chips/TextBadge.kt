package com.seucaio.unideas.ds.components.chips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.AppType
import com.seucaio.unideas.ds.theme.DsTheme

@Composable
fun TextBadge(text: String, background: Color, content: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        style = AppType.TypeBadge,
        color = content,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@PreviewLightDark
@Composable
private fun TextBadgePreview() {
    DsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            TextBadge(
                text = "TASK",
                background = MaterialTheme.colorScheme.primaryContainer,
                content = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
