package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.remote.model.SenderRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "sms_app_prefs")

class DataStoreManager(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    private val JWT_TOKEN = stringPreferencesKey("jwt_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val DEVICE_ID = stringPreferencesKey("device_id")
    private val SENDER_RULES = stringPreferencesKey("sender_rules")
    private val THEME_MODE = intPreferencesKey("theme_mode")

    val jwtTokenFlow: Flow<String?> = context.dataStore.data.map { it[JWT_TOKEN] }
    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN] }
    
    val senderRulesFlow: Flow<List<SenderRule>> = context.dataStore.data.map { 
        it[SENDER_RULES]?.let { rulesStr ->
            try { json.decodeFromString<List<SenderRule>>(rulesStr) } catch (e: Exception) { emptyList() }
        } ?: emptyList()
    }
    
    val deviceIdFlow: Flow<String?> = context.dataStore.data.map { it[DEVICE_ID] }
    val themeModeFlow: Flow<Int> = context.dataStore.data.map { it[THEME_MODE] ?: 0 }

    suspend fun getJwtToken(): String? = context.dataStore.data.first()[JWT_TOKEN]
    suspend fun getRefreshToken(): String? = context.dataStore.data.first()[REFRESH_TOKEN]

    suspend fun getDeviceId(): String {
        var id = context.dataStore.data.first()[DEVICE_ID]
        if (id == null) {
            id = UUID.randomUUID().toString()
            context.dataStore.edit { it[DEVICE_ID] = id!! }
        }
        return id
    }

    suspend fun getSenderRules(): List<SenderRule> {
        val rulesStr = context.dataStore.data.first()[SENDER_RULES]
        return if (rulesStr != null) {
            try { json.decodeFromString(rulesStr) } catch (e: Exception) { emptyList() }
        } else {
            emptyList()
        }
    }

    suspend fun setJwtToken(token: String?) {
        context.dataStore.edit {
            if (token != null) it[JWT_TOKEN] = token else it.remove(JWT_TOKEN)
        }
    }

    suspend fun setTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit {
            it[JWT_TOKEN] = accessToken
            it[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearTokens() {
        context.dataStore.edit {
            it.remove(JWT_TOKEN)
            it.remove(REFRESH_TOKEN)
        }
    }

    suspend fun setSenderRules(rules: List<SenderRule>) {
        context.dataStore.edit { it[SENDER_RULES] = json.encodeToString(rules) }
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }
}
