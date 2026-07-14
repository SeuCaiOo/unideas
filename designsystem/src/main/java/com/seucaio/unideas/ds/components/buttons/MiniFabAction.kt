package com.seucaio.unideas.ds.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seucaio.unideas.ds.theme.AccentContainer
import com.seucaio.unideas.ds.theme.Background
import com.seucaio.unideas.ds.theme.DsTheme
import com.seucaio.unideas.ds.theme.OnAccentContainer
import com.seucaio.unideas.ds.theme.Radii

@Composable
fun MiniFabAction(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(Radii.MiniFab))
            .background(AccentContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = OnAccentContainer, modifier = Modifier.size(20.dp))
        Text(label, color = OnAccentContainer, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@PreviewLightDark
@Composable
private fun MiniFabActionPreview() {
    DsTheme {
        Box(Modifier.background(Background).padding(16.dp)) {
            MiniFabAction(icon = Icons.Outlined.TaskAlt, label = "New task", onClick = {})
        }
    }
}
