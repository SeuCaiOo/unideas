package com.seucaio.unideas.ds.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.Background
import com.seucaio.unideas.ds.theme.DsTheme
import com.seucaio.unideas.ds.theme.SnackbarBackground
import com.seucaio.unideas.ds.theme.SnackbarContent

@Composable
fun AppSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            containerColor = SnackbarBackground,
            contentColor = SnackbarContent,
            shape = RoundedCornerShape(10.dp),
            snackbarData = data
        )
    }
}

private class PreviewSnackbarVisuals(override val message: String) : SnackbarVisuals {
    override val actionLabel: String? = null
    override val withDismissAction: Boolean = false
    override val duration = androidx.compose.material3.SnackbarDuration.Indefinite
}

@PreviewLightDark
@Composable
private fun AppSnackbarHostPreview() {
    DsTheme {
        Box(Modifier.background(Background).padding(16.dp)) {
            Snackbar(
                containerColor = SnackbarBackground,
                contentColor = SnackbarContent,
                shape = RoundedCornerShape(10.dp),
                snackbarData = object : SnackbarData {
                    override val visuals = PreviewSnackbarVisuals("Item deleted")
                    override fun performAction() {}
                    override fun dismiss() {}
                }
            )
        }
    }
}
