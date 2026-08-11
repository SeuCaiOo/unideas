package com.seucaio.unideas.feature.home.features.panel.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.components.buttons.AppFab
import com.seucaio.unideas.ds.components.buttons.MiniFabAction
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.home.R

@Composable
internal fun AddItemFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddTask: () -> Unit,
    onAddNote: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.End) {
        if (expanded) {
            MiniFabAction(
                icon = Icons.AutoMirrored.Outlined.Notes,
                label = stringResource(R.string.home_add_note),
                onClick = onAddNote,
            )
            Spacer(Modifier.height(8.dp))
            MiniFabAction(
                icon = Icons.Outlined.TaskAlt,
                label = stringResource(R.string.home_add_task),
                onClick = onAddTask
            )
            Spacer(Modifier.height(12.dp))
        }
        AppFab(
            icon = Icons.Outlined.Add,
            contentDescription = stringResource(R.string.home_add_action),
            onClick = onToggle
        )
    }
}

@PreviewLightDark
@Composable
private fun AddItemFabPreview() {
    UdsTheme {
        Surface {
            var expanded by remember { mutableStateOf(true) }
            AddItemFab(
                expanded = expanded,
                onToggle = { expanded = !expanded },
                onAddTask = {},
                onAddNote = {},
            )
        }
    }
}
