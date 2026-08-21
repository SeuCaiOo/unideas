package com.seucaio.unideas.feature.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.seucaio.unideas.ds.components.legacy.UnideasEmptyContent
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.onboarding.viewmodel.OnboardingEvent
import com.seucaio.unideas.feature.onboarding.viewmodel.OnboardingUiAction
import com.seucaio.unideas.feature.onboarding.viewmodel.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val resources by rememberUpdatedState(LocalResources.current)

    val signInLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val account = runCatching {
                GoogleSignIn.getSignedInAccountFromIntent(result.data).result
            }.getOrNull()
            viewModel.onEvent(OnboardingEvent.OnGoogleSignInResult(account))
        }

    LaunchedEffect(Unit) {
        viewModel.uiAction.collect { action ->
            when (action) {
                is OnboardingUiAction.LaunchGoogleSignIn -> signInLauncher.launch(action.intent)
                is OnboardingUiAction.ShowSnackbar ->
                    snackbarHostState.showSnackbar(resources.getString(action.messageRes))

                OnboardingUiAction.OnboardingComplete -> onOnboardingComplete()
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        OnboardingContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
            onConnectClick = { viewModel.onEvent(OnboardingEvent.OnConnectClicked) },
            onSkipClick = { viewModel.onEvent(OnboardingEvent.OnSkipClicked) },
        )
    }
}

@Composable
private fun OnboardingContent(
    onConnectClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TextButton(onClick = onSkipClick, modifier = Modifier.align(Alignment.End)) {
            Text(text = stringResource(R.string.onboarding_skip))
        }

        UnideasEmptyContent(
            titleRes = R.string.onboarding_title,
            messageRes = R.string.onboarding_body,
            icon = Icons.Outlined.CloudSync,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = onConnectClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.onboarding_connect))
            }

            Text(
                text = stringResource(R.string.onboarding_footnote),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingContentPreview() {
    UdsTheme {
        Surface {
            OnboardingContent(
                onConnectClick = {},
                onSkipClick = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
