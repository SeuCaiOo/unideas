package com.seucaio.unideas.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.common.dev.DevScreenVersionToggle
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.settings.viewmodel.SettingsUiState

/**
 * Dev-only toggle for #84 — picks V1 vs V2 per screen while both coexist. Reads/writes
 * [DevScreenVersionToggle] directly, orthogonal to [SettingsUiState] on purpose (same spirit as
 * the Seed/Clear database dev actions above).
 */
@Composable
internal fun UseV2ScreensRow() {
    val useV2 by DevScreenVersionToggle.useV2.collectAsStateWithLifecycle()
    UseV2ScreensRow(
        useV2 = useV2,
        onUseV2Change = DevScreenVersionToggle::set
    )
}

@Composable
private fun UseV2ScreensRow(
    useV2: Boolean,
    onUseV2Change: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.settings_debug_use_v2_screens))
        Switch(checked = useV2, onCheckedChange = onUseV2Change)
    }
}

@PreviewLightDark
@Composable
private fun UseV2ScreensRowPreview() {
    UdsTheme {
        Surface {
            UseV2ScreensRow(
                useV2 = true,
                onUseV2Change = {}
            )
        }
    }
}
