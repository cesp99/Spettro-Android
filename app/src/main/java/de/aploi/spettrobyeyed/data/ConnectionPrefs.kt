package de.aploi.spettrobyeyed.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "connection")

class ConnectionPrefs(private val context: Context) {
    companion object {
        private val KEY_HOST = stringPreferencesKey("host")
        private val KEY_API_KEY = stringPreferencesKey("api_key")
    }

    val host: Flow<String> = context.dataStore.data.map { it[KEY_HOST] ?: "" }
    val apiKey: Flow<String> = context.dataStore.data.map { it[KEY_API_KEY] ?: "" }

    suspend fun save(host: String, apiKey: String) {
        context.dataStore.edit {
            it[KEY_HOST] = host
            it[KEY_API_KEY] = apiKey
        }
    }

    suspend fun clear() {
        context.dataStore.edit {
            it.remove(KEY_HOST)
            it.remove(KEY_API_KEY)
        }
    }
}
