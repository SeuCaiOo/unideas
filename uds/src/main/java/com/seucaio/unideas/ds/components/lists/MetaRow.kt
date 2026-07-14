package com.seucaio.unideas.ds.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seucaio.unideas.ds.theme.AppType
import com.seucaio.unideas.ds.theme.Background
import com.seucaio.unideas.ds.theme.Danger
import com.seucaio.unideas.ds.theme.DividerColor
import com.seucaio.unideas.ds.theme.DsTheme
import com.seucaio.unideas.ds.theme.TextPrimary
import com.seucaio.unideas.ds.theme.TextTertiary

@Composable
fun MetaRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary,
    icon: ImageVector? = null,
    isLast: Boolean = false
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = AppType.Metadata, color = TextTertiary, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                }
                Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = valueColor)
            }
        }
        if (!isLast) HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@PreviewLightDark
@Composable
private fun MetaRowPreview() {
    DsTheme {
        Box(Modifier.background(Background).padding(16.dp)) {
            Column {
                MetaRow(label = "Section", value = "Home")
                MetaRow(label = "Due date", value = "6 days overdue", valueColor = Danger, isLast = true)
            }
        }
    }
}
