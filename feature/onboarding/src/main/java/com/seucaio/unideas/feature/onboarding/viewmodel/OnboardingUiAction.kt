package com.seucaio.unideas.feature.onboarding.viewmodel

import android.content.Intent
import androidx.annotation.StringRes
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.model.BackupInfo

sealed interface OnboardingUiAction {

    data class LaunchGoogleSignIn(val intent: Intent) : OnboardingUiAction

    data object OnboardingComplete : OnboardingUiAction

    data class ShowSnackbar(@param:StringRes val messageRes: Int) : OnboardingUiAction

    data class ShowRestoreBackupSheet(
        val account: GoogleSignInAccount,
        val backupInfo: BackupInfo,
    ) : OnboardingUiAction

    data object RestoreCompleted : OnboardingUiAction
}
