package com.seucaio.unideas.ds.components.legacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.UdsTheme

/** The only place red/amber are used in the UI — reserved exclusively for due-date urgency. */
@Composable
fun UrgencyIndicator(level: UrgencyIndicatorLevel, modifier: Modifier = Modifier) {
    val color = when (level) {
        UrgencyIndicatorLevel.OVERDUE -> MaterialTheme.colorScheme.error
        UrgencyIndicatorLevel.DUE_SOON -> LocalUdsExtendedColors.current.warning
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
    UdsTheme {
        Surface {
            UrgencyIndicator(level = UrgencyIndicatorLevel.OVERDUE)
        }
    }
}

@PreviewLightDark
@Composable
private fun UrgencyIndicatorDueSoonPreview() {
    UdsTheme {
        Surface {
            UrgencyIndicator(level = UrgencyIndicatorLevel.DUE_SOON)
        }
    }
}

@PreviewLightDark
@Composable
private fun UrgencyIndicatorNormalPreview() {
    UdsTheme {
        Surface {
            UrgencyIndicator(level = UrgencyIndicatorLevel.NORMAL)
        }
    }
}
