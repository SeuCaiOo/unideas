package com.seucaio.unideas.ds.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
import com.seucaio.unideas.ds.components.inputs.GridSelectionBottomSheet
import com.seucaio.unideas.ds.components.inputs.SelectionBottomSheet
import com.seucaio.unideas.ds.components.inputs.SwitchSection
import com.seucaio.unideas.ds.components.lists.ActionRow
import com.seucaio.unideas.ds.components.lists.GroupHeader
import com.seucaio.unideas.ds.components.lists.ManageListRow
import com.seucaio.unideas.ds.components.lists.MetaChipsRow
import com.seucaio.unideas.ds.components.lists.MetaRow
import com.seucaio.unideas.ds.components.lists.NavRow
import com.seucaio.unideas.ds.components.lists.item.ListItemRow
import com.seucaio.unideas.ds.components.lists.model.ListItemUi
import com.seucaio.unideas.ds.components.navigation.TabItem
import com.seucaio.unideas.ds.components.panels.PriorityPanel
import com.seucaio.unideas.ds.components.panels.PriorityRowUi
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.UdsTheme

/**
 * One screen, every component. Not part of the public API surface consumed by an app -
 * this exists purely as a visual reference for deciding what to port when adopting this
 * design system in a different project. Open in Android Studio's Split/Design view, or
 * run it directly (see [ComponentGalleryPreview]).
 *
 * Each category collapses independently via [CollapsibleGroupHeader] - starts collapsed so a
 * long component list stays navigable instead of one continuous scroll.
 */
@Composable
fun ComponentGallery(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Category("typography/") {
            Labeled("headlineLarge (item title, detail screen)") {
                Text(
                    "Renew car insurance",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Labeled("headlineMedium (list item title)") {
                Text(
                    "Pay electricity bill",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Labeled("headlineSmall (tab label)") {
                Text(
                    "Tasks",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Labeled("titleLarge (screen title)") {
                Text(
                    "Item detail",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Labeled("titleMedium (chip label)") {
                Text(
                    "urgent",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Labeled("titleSmall (metadata / subtitle)") {
                Text(
                    "Home · 6 items",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Labeled("bodyLarge (description body)") {
                Text(
                    "Don't forget to bring the documents.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Labeled("bodyMedium (due badge)") {
                Text(
                    "3 days overdue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Labeled("bodySmall (type badge)") {
                Text("TASK", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
            Labeled("labelLarge (button label)") {
                Text("SAVE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            Labeled("labelMedium (field label)") {
                Text("TITLE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Category("buttons/") {
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
                SegmentedControl(
                    options = listOf("Task", "Note"),
                    selectedIndex = selected,
                    onSelect = { selected = it }
                )
            }
        }

        Category("chips/") {
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
                    DueBadge(label = "due today", color = LocalUdsExtendedColors.current.warning)
                }
            }
        }

        Category("inputs/") {
            Labeled("AppTextField") {
                var text by remember { mutableStateOf("") }
                AppTextField(value = text, onValueChange = { text = it }, placeholder = "e.g. Pay electricity bill")
            }
            Labeled("FormField (label + slot)") {
                FormField(
                    label = "Title"
                ) { Text("any content goes here", color = MaterialTheme.colorScheme.onSurface) }
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
            Labeled("SwitchSection") {
                var checked by remember { mutableStateOf(true) }
                SwitchSection(label = "Reminder", checked = checked, onCheckedChange = { checked = it }) {
                    Text(
                        "Revealed content",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
            Labeled("SelectionBottomSheet") {
                var selected by remember { mutableStateOf("Weekly") }
                var showSheet by remember { mutableStateOf(false) }
                DateFieldButton(
                    valueLabel = selected,
                    onClick = { showSheet = true },
                    onClear = {},
                    clearContentDescription = "Clear"
                )
                if (showSheet) {
                    SelectionBottomSheet(
                        title = "Repeat",
                        options = listOf("Daily", "Weekly", "Monthly"),
                        selectedOption = selected,
                        optionLabel = { it },
                        onOptionSelected = {
                            selected = it
                            showSheet = false
                        },
                        onDismiss = { showSheet = false },
                    )
                }
            }
            Labeled("GridSelectionBottomSheet") {
                var selectedDay by remember { mutableIntStateOf(15) }
                var showDaySheet by remember { mutableStateOf(false) }
                DateFieldButton(
                    valueLabel = selectedDay.toString(),
                    onClick = { showDaySheet = true },
                    onClear = {},
                    clearContentDescription = "Clear"
                )
                if (showDaySheet) {
                    GridSelectionBottomSheet(
                        title = "Which day of the month?",
                        options = (1..31).toList(),
                        selectedOption = selectedDay,
                        optionLabel = { it.toString() },
                        onOptionSelected = {
                            selectedDay = it
                            showDaySheet = false
                        },
                        onDismiss = { showDaySheet = false },
                    )
                }
            }
            Labeled("DateFieldButton") {
                DateFieldButton(
                    valueLabel = "Jul 14, 2026",
                    onClick = {},
                    onClear = {},
                    clearContentDescription = "Clear"
                )
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
        }

        Category("lists/") {
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
        }

        Category("navigation/") {
            Labeled("TabItem") {
                Row(Modifier.fillMaxWidth()) {
                    TabItem(label = "Tasks", selected = true, onClick = {}, modifier = Modifier.weight(1f))
                    TabItem(label = "Notes", selected = false, onClick = {}, modifier = Modifier.weight(1f))
                }
            }
        }

        Category("panels/") {
            Labeled("PriorityPanel (+ PriorityRowUi)") {
                PriorityPanel(
                    title = "Priorities",
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
                            badgeColor = LocalUdsExtendedColors.current.warning
                        )
                    ),
                    footerLabel = "view all (6)",
                    onFooterClick = {},
                    onRowClick = {}
                )
            }
        }

        Category("feedback/") {
            Labeled("AppSnackbarHost (static preview of the Snackbar shape)") {
                val hostState = remember { SnackbarHostState() }
                AppSnackbarHost(hostState = hostState, modifier = Modifier.fillMaxWidth())
                Text(
                    "(shows nothing until a snackbar is triggered - see any screen for a live example)",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun Category(title: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "chevronRotation")

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(top = 20.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.rotate(rotation),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column { content() }
        }
    }
}

@Composable
private fun Labeled(name: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(bottom = 20.dp)) {
        Text(
            name.uppercase(),
            color = LocalUdsExtendedColors.current.textTertiary,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        content()
    }
}

@PreviewLightDark
@Composable
private fun ComponentGalleryPreview() {
    UdsTheme { ComponentGallery() }
}
