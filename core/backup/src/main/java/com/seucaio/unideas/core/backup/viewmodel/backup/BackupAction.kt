package com.seucaio.unideas.core.backup.viewmodel.backup

sealed interface BackupAction {
    data object Connect : BackupAction
    data object Upload : BackupAction
    data object Sync : BackupAction
}
