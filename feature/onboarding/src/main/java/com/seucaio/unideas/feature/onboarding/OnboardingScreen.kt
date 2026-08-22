package com.seucaio.unideas.feature.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.LowPriority
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.common.extensions.restartApplication
import com.seucaio.unideas.core.common.extensions.toFormattedDateTimeString
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.onboarding.viewmodel.OnboardingEvent
import com.seucaio.unideas.feature.onboarding.viewmodel.OnboardingUiAction
import com.seucaio.unideas.feature.onboarding.viewmodel.OnboardingViewModel
import org.koin.androidx.compose.koinViewModel

private const val ICON_BACKGROUND_ALPHA = 0.14f

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val resources by rememberUpdatedState(LocalResources.current)
    val context = LocalContext.current

    var restoreSheetData by remember { mutableStateOf<Pair<String?, BackupInfo>?>(null) }

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

                is OnboardingUiAction.ShowRestoreBackupSheet ->
                    restoreSheetData = action.account.email to action.backupInfo

                OnboardingUiAction.RestoreCompleted -> context.restartApplication()
            }
        }
    }

    OnboardingContent(
        snackbarHostState = snackbarHostState,
        onConnectClick = { viewModel.onEvent(OnboardingEvent.OnConnectClicked) },
        onSkipClick = { viewModel.onEvent(OnboardingEvent.OnSkipClicked) },
    )

    restoreSheetData?.let { (accountEmail, backupInfo) ->
        RestoreBackupBottomSheet(
            accountEmail = accountEmail,
            backupCreatedAt = backupInfo.createdAt.toFormattedDateTimeString(),
            onRestoreClick = {
                restoreSheetData = null
                viewModel.onEvent(OnboardingEvent.OnRestoreBackupConfirmed(backupInfo.fileId))
            },
            onStartFreshClick = {
                restoreSheetData = null
                viewModel.onEvent(OnboardingEvent.OnStartFreshClicked)
            },
        )
    }
}

@Composable
private fun OnboardingContent(
    snackbarHostState: SnackbarHostState,
    onConnectClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = onConnectClick, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.onboarding_connect))
                }

                OutlinedButton(onClick = onSkipClick) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(
                    text = stringResource(R.string.onboarding_footnote),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        OnboardingBody(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
        )
    }
}

@Composable
private fun OnboardingIconBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = ICON_BACKGROUND_ALPHA)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_app_logo),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
    }
}

@Composable
private fun OnboardingFeatureRow(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun OnboardingBody(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OnboardingIconBadge()
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 24.dp),
            )
            Text(
                text = stringResource(R.string.onboarding_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
            Column(
                modifier = Modifier.padding(top = 42.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                OnboardingFeatureRow(
                    icon = Icons.Outlined.LowPriority,
                    text = stringResource(R.string.onboarding_feature_priorities),
                )
                OnboardingFeatureRow(
                    icon = Icons.Outlined.Label,
                    text = stringResource(R.string.onboarding_feature_sections_tags),
                )
                OnboardingFeatureRow(
                    icon = Icons.Outlined.Notifications,
                    text = stringResource(R.string.onboarding_feature_reminders),
                )
                OnboardingFeatureRow(
                    icon = Icons.Outlined.CloudDone,
                    text = stringResource(R.string.onboarding_feature_backup),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingContentPreview() {
    UdsTheme {
        OnboardingContent(
            snackbarHostState = remember { SnackbarHostState() },
            onConnectClick = {},
            onSkipClick = {},
        )
    }
}
