package com.seucaio.unideas.ds.components.legacy

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.ds.theme.UdsTheme

enum class UnideasTopBarVariant {
    Standard, Large
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnideasTopBar(
    modifier: Modifier = Modifier,
    title: String = "",
    onNavigateBack: (() -> Unit)? = null,
    navigationBackIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    variant: UnideasTopBarVariant = UnideasTopBarVariant.Standard,
    containerColor: Color = MaterialTheme.colorScheme.background,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val navigationIcon: @Composable () -> Unit = {
        if (onNavigateBack != null) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = navigationBackIcon, contentDescription = null)
            }
        }
    }

    when (variant) {
        UnideasTopBarVariant.Standard -> TopAppBar(
            title = { Text(title) },
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor),
            modifier = modifier,
        )
        UnideasTopBarVariant.Large -> LargeTopAppBar(
            title = { Text(title) },
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor),
            modifier = modifier,
        )
    }
}

@PreviewLightDark
@Composable
private fun UnideasTopBarPreview() {
    UdsTheme {
        Surface {
            UnideasTopBar(title = "Unideas")
        }
    }
}

@PreviewLightDark
@Composable
private fun UnideasTopBarWithBackPreview() {
    UdsTheme {
        Surface {
            UnideasTopBar(title = "Detalhe", onNavigateBack = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun UnideasTopBarLargePreview() {
    UdsTheme {
        Surface {
            UnideasTopBar(title = "Detalhe", onNavigateBack = {}, variant = UnideasTopBarVariant.Large)
        }
    }
}
