package com.seucaio.unideas.core.backup

import androidx.annotation.StringRes
import com.seucaio.unideas.core.backup.viewmodel.BackupEvent
import com.seucaio.unideas.core.backup.viewmodel.PendingBackupOverwrite

internal sealed interface PendingConfirmDialog {
    @get:StringRes val titleRes: Int

    @get:StringRes val messageRes: Int

    @get:StringRes val confirmActionRes: Int

    fun toEvent(): BackupEvent

    data class Delete(val fileId: String) : PendingConfirmDialog {
        override val titleRes = R.string.backup_delete_confirm_title
        override val messageRes = R.string.backup_delete_confirm_message
        override val confirmActionRes = R.string.backup_delete_confirm_action
        override fun toEvent(): BackupEvent = BackupEvent.OnDeleteConfirmed(fileId)
    }

    data class Overwrite(val pending: PendingBackupOverwrite) : PendingConfirmDialog {
        override val titleRes = R.string.backup_overwrite_confirm_title
        override val messageRes = R.string.backup_overwrite_confirm_message
        override val confirmActionRes = R.string.backup_overwrite_confirm_action
        override fun toEvent(): BackupEvent = BackupEvent.OnOverwriteConfirmClicked(pending)
    }
}
