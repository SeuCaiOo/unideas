package com.seucaio.unideas.feature.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.domain.model.SeedScope
import com.seucaio.unideas.ds.theme.UdsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedScopeBottomSheet(
    selectedScope: SeedScope?,
    onScopeSelect: (SeedScope) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        SeedScopeSheetContent(
            selectedScope = selectedScope,
            onScopeSelect = onScopeSelect,
            onConfirm = onConfirm,
        )
    }
}

@Composable
private fun SeedScopeSheetContent(
    selectedScope: SeedScope?,
    onScopeSelect: (SeedScope) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.seed_scope_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Column(modifier = Modifier.selectableGroup()) {
            SeedScope.entries.forEachIndexed { index, scope ->
                SeedScopeOption(
                    scope = scope,
                    selected = selectedScope == scope,
                    onSelect = { onScopeSelect(scope) },
                )
                if (index < SeedScope.entries.lastIndex) {
                    HorizontalDivider()
                }
            }
        }

        Button(
            onClick = onConfirm,
            enabled = selectedScope != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Text(text = stringResource(R.string.seed_scope_confirm))
        }
    }
}

@Composable
private fun SeedScopeOption(
    scope: SeedScope,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(scope.labelRes),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(scope.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@get:StringRes
private val SeedScope.labelRes: Int
    get() = when (this) {
        SeedScope.EMPTY -> R.string.seed_scope_empty
        SeedScope.BASIC -> R.string.seed_scope_basic
        SeedScope.FULL -> R.string.seed_scope_full
    }

@get:StringRes
private val SeedScope.descriptionRes: Int
    get() = when (this) {
        SeedScope.EMPTY -> R.string.seed_scope_empty_description
        SeedScope.BASIC -> R.string.seed_scope_basic_description
        SeedScope.FULL -> R.string.seed_scope_full_description
    }

@PreviewLightDark
@Composable
private fun SeedScopeSheetContentPreview() {
    UdsTheme {
        Surface {
            SeedScopeSheetContent(
                selectedScope = SeedScope.FULL,
                onScopeSelect = {},
                onConfirm = {},
            )
        }
    }
}
