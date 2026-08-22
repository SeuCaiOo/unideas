package com.seucaio.unideas.core.backup.viewmodel

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

data class AccountUiState(
    val isConnected: Boolean = false,
    val accountName: String? = null,
    val accountEmail: String? = null,
) {

    fun withAccount(account: GoogleSignInAccount?): AccountUiState = copy(
        isConnected = account != null,
        accountName = account?.displayName,
        accountEmail = account?.email,
    )
}
