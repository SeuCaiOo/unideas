package com.seucaio.unideas.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_prefs")

private val KEY_ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")

class OnboardingPreferences(private val context: Context) {

    suspend fun isSeen(): Boolean =
        context.onboardingDataStore.data.first()[KEY_ONBOARDING_SEEN] ?: false

    suspend fun setSeen(seen: Boolean) {
        context.onboardingDataStore.edit { prefs -> prefs[KEY_ONBOARDING_SEEN] = seen }
    }
}
