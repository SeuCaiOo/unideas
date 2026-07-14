package com.seucaio.unideas.ds.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seucaio.unideas.ds.components.buttons.AppFab
import com.seucaio.unideas.ds.components.buttons.AppIconButton
import com.seucaio.unideas.ds.components.buttons.MiniFabAction
import com.seucaio.unideas.ds.components.buttons.SegmentedControl
import com.seucaio.unideas.ds.components.chips.DueBadge
import com.seucaio.unideas.ds.components.chips.RemovableChip
import com.seucaio.unideas.ds.components.chips.SelectableChip
import com.seucaio.unideas.ds.components.chips.TextBadge
import com.seucaio.unideas.ds.components.feedback.AppSnackbarHost
import com.seucaio.unideas.ds.components.inputs.AddEntryRow
import com.seucaio.unideas.ds.components.inputs.AppTextField
import com.seucaio.unideas.ds.components.inputs.DateFieldButton
import com.seucaio.unideas.ds.components.inputs.DropdownField
import com.seucaio.unideas.ds.components.inputs.FilterDropdownPill
import com.seucaio.unideas.ds.components.inputs.FormField
import com.seucaio.unideas.ds.components.lists.ActionRow
import com.seucaio.unideas.ds.components.lists.GroupHeader
import com.seucaio.unideas.ds.components.lists.ListItemRow
import com.seucaio.unideas.ds.components.lists.ListItemUi
import com.seucaio.unideas.ds.components.lists.ManageListRow
import com.seucaio.unideas.ds.components.lists.MetaChipsRow
import com.seucaio.unideas.ds.components.lists.MetaRow
import com.seucaio.unideas.ds.components.lists.NavRow
import com.seucaio.unideas.ds.components.navigation.TabItem
import com.seucaio.unideas.ds.components.panels.PriorityPanel
import com.seucaio.unideas.ds.components.panels.PriorityRowUi
import com.seucaio.unideas.ds.theme.DsTheme
import com.seucaio.unideas.ds.theme.LocalDsExtendedColors

/**
 * One screen, every component. Not part of the public API surface consumed by an app -
 * this exists purely as a visual reference for deciding what to port when adopting this
 * design system in a different project. Open in Android Studio's Split/Design view, or
 * run it directly (see [ComponentGalleryPreview]).
 *
 * File path next to each label is where to find that component under uds/src/.
 */
@Composable
fun ComponentGallery() {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CategoryTitle("buttons/")
        Labeled("AppIconButton") {
            AppIconButton(icon = Icons.Outlined.Settings, contentDescription = "Settings", onClick = {})
        }
        Labeled("AppFab") {
            AppFab(icon = Icons.Outlined.Add, contentDescription = "Add", onClick = {})
        }
        Labeled("MiniFabAction") {
            MiniFabAction(icon = Icons.Outlined.TaskAlt, label = "New task", onClick = {})
        }
        Labeled("SegmentedControl") {
            var selected by remember { mutableIntStateOf(0) }
            SegmentedControl(options = listOf("Task", "Note"), selectedIndex = selected, onSelect = { selected = it })
        }

        CategoryTitle("chips/")
        Labeled("SelectableChip") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectableChip(label = "urgent", selected = true, onClick = {})
                SelectableChip(label = "market", selected = false, onClick = {})
            }
        }
        Labeled("RemovableChip") {
            RemovableChip(label = "android", onRemove = {}, removeContentDescription = "Remove")
        }
        Labeled("TextBadge") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextBadge(
                    text = "TASK",
                    background = MaterialTheme.colorScheme.primaryContainer,
                    content = MaterialTheme.colorScheme.onPrimaryContainer
                )
                TextBadge(
                    text = "DONE",
                    background = MaterialTheme.colorScheme.background,
                    content = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Labeled("DueBadge") {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DueBadge(label = "3 days overdue", color = MaterialTheme.colorScheme.error)
                DueBadge(label = "due today", color = LocalDsExtendedColors.current.warning)
            }
        }

        CategoryTitle("inputs/")
        Labeled("AppTextField") {
            var text by remember { mutableStateOf("") }
            AppTextField(value = text, onValueChange = { text = it }, placeholder = "e.g. Pay electricity bill")
        }
        Labeled("FormField (label + slot)") {
            FormField(label = "Title") { Text("any content goes here", color = MaterialTheme.colorScheme.onSurface) }
        }
        Labeled("DropdownField") {
            var selected by remember { mutableStateOf("") }
            DropdownField(
                options = listOf("Personal", "Work"),
                selected = selected,
                emptyOptionLabel = "No section",
                onSelect = { selected = it }
            )
        }
        Labeled("FilterDropdownPill") {
            var selected by remember { mutableStateOf("") }
            FilterDropdownPill(
                options = listOf("Personal", "Work"),
                selected = selected,
                allOptionLabel = "All sections",
                onSelect = { selected = it }
            )
        }
        Labeled("DateFieldButton") {
            DateFieldButton(valueLabel = "Jul 14, 2026", onClick = {}, onClear = {}, clearContentDescription = "Clear")
        }
        Labeled("AddEntryRow") {
            var text by remember { mutableStateOf("") }
            AddEntryRow(
                value = text,
                onValueChange = { text = it },
                placeholder = "New section...",
                addContentDescription = "Add",
                onSubmit = {}
            )
        }

        CategoryTitle("lists/")
        Labeled("ListItemRow (+ ListItemUi)") {
            ListItemRow(
                ui = ListItemUi(
                    id = 1L, title = "Pay electricity bill", meta = "Home", showCheckbox = true,
                    checked = false, showRepeatIcon = true, badgeLabel = "6 days overdue",
                    badgeColor = MaterialTheme.colorScheme.error, checkContentDescription = "Confirm"
                ),
                onClick = {},
                onToggleCheck = {}
            )
        }
        Labeled("ManageListRow") {
            ManageListRow(icon = Icons.Outlined.Folder, title = "Personal", subtitle = "5 items") {
                AppIconButton(
                    icon = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    buttonSize = 40.dp,
                    iconSize = 20.dp,
                    onClick = {}
                )
            }
        }
        Labeled("MetaRow") {
            Column {
                MetaRow(
                    label = "Section",
                    value = "Home"
                )
                MetaRow(
                    label = "Due date",
                    value = "6 days overdue",
                    valueColor = MaterialTheme.colorScheme.error,
                    isLast = true
                )
            }
        }
        Labeled("MetaChipsRow") {
            MetaChipsRow(label = "Tags", chips = listOf("urgent", "bills"))
        }
        Labeled("ActionRow") {
            ActionRow(
                icon = Icons.Outlined.Backup,
                iconTint = MaterialTheme.colorScheme.primary,
                label = "Back up now",
                onClick = {}
            )
        }
        Labeled("NavRow") {
            NavRow(icon = Icons.Outlined.Label, label = "Tags", onClick = {})
        }
        Labeled("GroupHeader") {
            GroupHeader("Account")
        }

        CategoryTitle("navigation/")
        Labeled("TabItem") {
            Row(Modifier.fillMaxWidth()) {
                TabItem(label = "Tasks", selected = true, onClick = {}, modifier = Modifier.weight(1f))
                TabItem(label = "Notes", selected = false, onClick = {}, modifier = Modifier.weight(1f))
            }
        }

        CategoryTitle("panels/")
        Labeled("PriorityPanel (+ PriorityRowUi)") {
            PriorityPanel(
                title = "Priorities",
                icon = Icons.Outlined.Flag,
                rows = listOf(
                    PriorityRowUi(
                        id = 1L,
                        title = "Pay electricity bill",
                        badgeLabel = "6 days overdue",
                        badgeColor = MaterialTheme.colorScheme.error
                    ),
                    PriorityRowUi(
                        id = 2L,
                        title = "Morning stretch",
                        badgeLabel = "due today",
                        badgeColor = LocalDsExtendedColors.current.warning
                    )
                ),
                footerLabel = "view all (6)",
                onFooterClick = {},
                onRowClick = {}
            )
        }

        CategoryTitle("feedback/")
        Labeled("AppSnackbarHost (static preview of the Snackbar shape)") {
            val hostState = remember { SnackbarHostState() }
            AppSnackbarHost(hostState = hostState, modifier = Modifier.fillMaxWidth())
            Text(
                "(shows nothing until a snackbar is triggered - see any screen for a live example)",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun CategoryTitle(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun Labeled(name: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(bottom = 14.dp)) {
        Text(
            name,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        content()
    }
}

@PreviewLightDark
@Composable
private fun ComponentGalleryPreview() {
    DsTheme { ComponentGallery() }
}
