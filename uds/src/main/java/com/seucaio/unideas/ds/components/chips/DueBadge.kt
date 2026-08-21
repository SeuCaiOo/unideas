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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.UdsTheme

private const val BADGE_BACKGROUND_ALPHA = 0.16f

@Composable
fun DueBadge(label: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        label,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(color.copy(alpha = BADGE_BACKGROUND_ALPHA))
            .padding(horizontal = 9.dp, vertical = 5.dp),
    )
}

private enum class DueBadgePreviewScenario { Overdue, DueSoon, Normal }

private class DueBadgePreviewProvider : PreviewParameterProvider<DueBadgePreviewScenario> {
    override val values = DueBadgePreviewScenario.entries.asSequence()
}

@PreviewLightDark
@Composable
private fun DueBadgePreview(
    @PreviewParameter(DueBadgePreviewProvider::class) scenario: DueBadgePreviewScenario,
) {
    UdsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            when (scenario) {
                DueBadgePreviewScenario.Overdue ->
                    DueBadge(label = "3 days overdue", color = MaterialTheme.colorScheme.error)
                DueBadgePreviewScenario.DueSoon ->
                    DueBadge(label = "due today", color = LocalUdsExtendedColors.current.warning)
                DueBadgePreviewScenario.Normal ->
                    DueBadge(label = "due in 12 days", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
