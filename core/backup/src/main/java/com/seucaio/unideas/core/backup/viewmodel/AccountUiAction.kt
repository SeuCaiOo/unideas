package com.seucaio.unideas.core.backup.viewmodel

import android.content.Intent
import androidx.annotation.StringRes

sealed interface AccountUiAction {
    data class ShowSnackbar(@param:StringRes val message: Int) : AccountUiAction
    data class LaunchGoogleSignIn(val intent: Intent) : AccountUiAction
    data object SignInCompleted : AccountUiAction
    data object SignedOut : AccountUiAction
}
