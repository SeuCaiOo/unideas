package com.seucaio.unideas.ds.components.legacy

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.ds.theme.UdsTheme

/** [UnideasTopBarVariant.Standard] is today's compact single-line bar; [UnideasTopBarVariant.Large] opts into Material3's `LargeTopAppBar` (bigger 2-line title). No scroll-collapse behavior wired up — no screen connects a nested scroll to it today. */
enum class UnideasTopBarVariant {
    Standard, Large
}

@Composable
fun UnideasTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    navigationBackIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    variant: UnideasTopBarVariant = UnideasTopBarVariant.Standard,
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
        UnideasTopBarVariant.Standard -> TopAppBarCompat(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            modifier = modifier,
        )
        UnideasTopBarVariant.Large -> LargeTopAppBarCompat(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarCompat(
    title: String,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LargeTopAppBarCompat(
    title: String,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier,
) {
    LargeTopAppBar(
        title = { Text(title) },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.largeTopAppBarColors(),
        modifier = modifier,
    )
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
