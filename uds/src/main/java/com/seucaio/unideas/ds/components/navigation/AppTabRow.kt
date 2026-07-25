package com.seucaio.unideas.ds.components.navigation

import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.ds.theme.AppType
import com.seucaio.unideas.ds.theme.UdsTheme

@Composable
fun AppTabRow(tabs: List<String>, selectedIndex: Int, onTabSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    SecondaryTabRow(selectedTabIndex = selectedIndex, modifier = modifier) {
        tabs.forEachIndexed { index, label ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onTabSelected(index) },
                text = { Text(label, style = AppType.TabLabel) },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun AppTabRowPreview() {
    UdsTheme {
        Surface {
            AppTabRow(tabs = listOf("Tasks", "Notes"), selectedIndex = 0, onTabSelected = {})
        }
    }
}
