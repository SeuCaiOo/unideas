package com.seucaio.unideas.core.backup.domain.usecase

class AutoBackupSettingsUseCase(
    private val getAutoBackupEnabledUseCase: GetAutoBackupEnabledUseCase,
    private val setAutoBackupEnabledUseCase: SetAutoBackupEnabledUseCase,
    private val getAutoBackupTrackedFileIdUseCase: GetAutoBackupTrackedFileIdUseCase,
) {

    suspend fun isEnabled(): Boolean = getAutoBackupEnabledUseCase()

    suspend fun setEnabled(enabled: Boolean) = setAutoBackupEnabledUseCase(enabled)

    suspend fun getTrackedFileId(): String? = getAutoBackupTrackedFileIdUseCase()
}
