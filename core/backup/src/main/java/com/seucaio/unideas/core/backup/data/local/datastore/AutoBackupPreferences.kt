package com.seucaio.unideas.core.backup.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.autoBackupDataStore by preferencesDataStore(name = "auto_backup_prefs")

private val KEY_ENABLED = booleanPreferencesKey("auto_backup_enabled")
private val KEY_TRACKED_FILE_ID = stringPreferencesKey("auto_backup_tracked_file_id")

class AutoBackupPreferences(private val context: Context) {

    suspend fun isEnabled(): Boolean =
        context.autoBackupDataStore.data.first()[KEY_ENABLED] ?: false

    suspend fun setEnabled(enabled: Boolean) {
        context.autoBackupDataStore.edit { prefs -> prefs[KEY_ENABLED] = enabled }
    }

    /** The Drive `fileId` of the current auto-backup slot, or null if none was uploaded yet. */
    suspend fun getTrackedFileId(): String? =
        context.autoBackupDataStore.data.first()[KEY_TRACKED_FILE_ID]

    suspend fun setTrackedFileId(fileId: String?) {
        context.autoBackupDataStore.edit { prefs ->
            if (fileId != null) prefs[KEY_TRACKED_FILE_ID] = fileId else prefs.remove(KEY_TRACKED_FILE_ID)
        }
    }
}
