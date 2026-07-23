package com.seucaio.unideas.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seucaio.unideas.core.common.dev.DevScreenVersionToggle
import com.seucaio.unideas.core.common.dev.ScreenVersion
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * Dev-only version picker for #84 — chooses which POC implementation is active for a screen
 * while several coexist. Reads/writes [DevScreenVersionToggle] directly, orthogonal to
 * [com.seucaio.unideas.feature.settings.viewmodel.SettingsUiState] on purpose (same spirit as the
 * Seed/Clear database dev actions above).
 */
@Composable
internal fun ScreenVersionRow() {
    val selectedVersion by DevScreenVersionToggle.selectedVersion.collectAsStateWithLifecycle()
    ScreenVersionRow(
        selectedVersion = selectedVersion,
        onVersionSelect = DevScreenVersionToggle::select,
    )
}

@Composable
private fun ScreenVersionRow(
    selectedVersion: ScreenVersion,
    onVersionSelect: (ScreenVersion) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.settings_debug_screen_version),
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ScreenVersion.entries.forEach { version ->
                ScreenVersionOption(
                    version = version,
                    selected = version == selectedVersion,
                    onSelect = { onVersionSelect(version) },
                )
            }
        }
    }
}

@Composable
private fun ScreenVersionOption(
    version: ScreenVersion,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = version.name, style = MaterialTheme.typography.bodyMedium)
    }
}

@PreviewLightDark
@Composable
private fun ScreenVersionRowPreview() {
    UdsTheme {
        Surface {
            ScreenVersionRow(
                selectedVersion = ScreenVersion.V3,
                onVersionSelect = {},
            )
        }
    }
}
