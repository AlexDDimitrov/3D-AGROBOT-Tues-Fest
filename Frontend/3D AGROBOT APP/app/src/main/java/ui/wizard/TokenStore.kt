package ui.wizard

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "auth")

object TokenStore {
    private val TOKEN_KEY = stringPreferencesKey("token")
    private val FIRST_NAME_KEY = stringPreferencesKey("first_name")
    private val LAST_NAME_KEY = stringPreferencesKey("last_name")

    suspend fun saveToken(context: Context, token: String, firstName: String, lastName: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[FIRST_NAME_KEY] = firstName
            prefs[LAST_NAME_KEY] = lastName
        }
    }
    suspend fun getToken(context: Context): String? {
            return context.dataStore.data
                .map { prefs -> prefs[TOKEN_KEY] }
                .first()
    }
    suspend fun clearToken(context: Context) {
            context.dataStore.edit { prefs ->
                prefs.remove(TOKEN_KEY)
        }
    }
    suspend fun getFirstName(context: Context): String? =
        context.dataStore.data.map { prefs -> prefs[FIRST_NAME_KEY] }.first()

    suspend fun getLastName(context: Context): String? =
        context.dataStore.data.map { prefs -> prefs[LAST_NAME_KEY] }.first()
}