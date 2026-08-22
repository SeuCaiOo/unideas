package com.seucaio.unideas.core.backup.viewmodel

import android.content.Intent
import androidx.annotation.StringRes
import com.seucaio.unideas.core.backup.domain.model.BackupInfo

sealed interface AccountUiAction {
    data class ShowSnackbar(@param:StringRes val message: Int) : AccountUiAction

    data object ShowSwitchAccountDialog : AccountUiAction
    data class LaunchSwitchAccountSignIn(val intent: Intent) : AccountUiAction
    data class ShowSwitchAccountRestoreSheet(val backupInfo: BackupInfo) : AccountUiAction

    data class SwitchAccountCompleted(val restarted: Boolean) : AccountUiAction

    data object ShowLogoutDialog : AccountUiAction
    data object LogoutCompleted : AccountUiAction
}
