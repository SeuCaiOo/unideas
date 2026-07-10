package com.seucaio.unideas.core.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.seucaio.unideas.core.ui.theme.UnideasTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnideasTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(),
        modifier = modifier,
    )
}

@Preview
@Composable
private fun UnideasTopBarPreview() {
    UnideasTheme {
        UnideasTopBar(title = "Unideas")
    }
}

@Preview
@Composable
private fun UnideasTopBarWithBackPreview() {
    UnideasTheme {
        UnideasTopBar(title = "Detalhe", onNavigateBack = {})
    }
}
