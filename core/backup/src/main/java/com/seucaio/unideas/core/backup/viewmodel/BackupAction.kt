package com.seucaio.unideas.core.backup.viewmodel

/** What to do once the pending Google Sign-In flow resolves. */
sealed interface BackupAction {
    data object Connect : BackupAction
    data object Upload : BackupAction
    data object Sync : BackupAction
}
