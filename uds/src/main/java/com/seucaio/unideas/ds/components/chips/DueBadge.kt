package com.seucaio.unideas.ds.components.chips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.AppType
import com.seucaio.unideas.ds.theme.DsTheme

@Composable
fun DueBadge(label: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = AppType.DueBadge, color = color)
    }
}

@PreviewLightDark
@Composable
private fun DueBadgePreview() {
    DsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            DueBadge(label = "3 days overdue", color = MaterialTheme.colorScheme.error)
        }
    }
}
