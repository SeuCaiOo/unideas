package com.seucaio.unideas.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.core.ui.theme.UnideasTheme
import com.seucaio.unideas.core.ui.theme.UrgencyColors

/** The only place red/amber are used in the UI — reserved exclusively for due-date urgency. */
@Composable
fun UrgencyIndicator(level: UrgencyIndicatorLevel, modifier: Modifier = Modifier) {
    val color = when (level) {
        UrgencyIndicatorLevel.OVERDUE -> UrgencyColors.Overdue
        UrgencyIndicatorLevel.DUE_SOON -> UrgencyColors.DueSoon
        UrgencyIndicatorLevel.NORMAL -> Color.Transparent
    }
    Box(
        modifier = modifier
            .size(10.dp)
            .background(color = color, shape = CircleShape),
    )
}

@PreviewLightDark
@Composable
private fun UrgencyIndicatorOverduePreview() {
    UnideasTheme {
        Surface {
            UrgencyIndicator(level = UrgencyIndicatorLevel.OVERDUE)
        }
    }
}

@PreviewLightDark
@Composable
private fun UrgencyIndicatorDueSoonPreview() {
    UnideasTheme {
        Surface {
            UrgencyIndicator(level = UrgencyIndicatorLevel.DUE_SOON)
        }
    }
}

@PreviewLightDark
@Composable
private fun UrgencyIndicatorNormalPreview() {
    UnideasTheme {
        Surface {
            UrgencyIndicator(level = UrgencyIndicatorLevel.NORMAL)
        }
    }
}
