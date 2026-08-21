package com.seucaio.unideas.ds.components.legacy

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme

private const val ICON_BACKGROUND_ALPHA = 0.14f

@Composable
fun UnideasEmptyContent(
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int? = null,
) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = ICON_BACKGROUND_ALPHA)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.TaskAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
            }
            if (titleRes != null) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 20.dp),
                )
                Text(
                    text = stringResource(messageRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp),
                )
            } else {
                Text(
                    text = stringResource(messageRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 20.dp),
                )
            }
        }
    }
}

private enum class UnideasEmptyContentPreviewScenario { MessageOnly, TitleAndMessage }

private class UnideasEmptyContentPreviewProvider : PreviewParameterProvider<UnideasEmptyContentPreviewScenario> {
    override val values = UnideasEmptyContentPreviewScenario.entries.asSequence()
}

@PreviewLightDark
@Composable
private fun UnideasEmptyContentPreview(
    @PreviewParameter(UnideasEmptyContentPreviewProvider::class) scenario: UnideasEmptyContentPreviewScenario,
) {
    UdsTheme {
        Surface {
            when (scenario) {
                UnideasEmptyContentPreviewScenario.MessageOnly ->
                    UnideasEmptyContent(messageRes = android.R.string.ok)
                UnideasEmptyContentPreviewScenario.TitleAndMessage ->
                    UnideasEmptyContent(titleRes = android.R.string.ok, messageRes = android.R.string.cancel)
            }
        }
    }
}
