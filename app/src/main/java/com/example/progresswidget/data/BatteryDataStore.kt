package com.example.progresswidget.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.progresswidget.model.BatteryEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "battery_settings")

class BatteryDataStore(private val context: Context) {

    companion object {
        private val CHARGER_CONNECTED_ENABLED = booleanPreferencesKey("charger_connected_enabled")
        private val CHARGER_CONNECTED_TEXT = stringPreferencesKey("charger_connected_text")
        
        private val CHARGER_DISCONNECTED_ENABLED = booleanPreferencesKey("charger_disconnected_enabled")
        private val CHARGER_DISCONNECTED_TEXT = stringPreferencesKey("charger_disconnected_text")
        
        private val BATTERY_LOW_ENABLED = booleanPreferencesKey("battery_low_enabled")
        private val BATTERY_LOW_TEXT = stringPreferencesKey("battery_low_text")
        private val BATTERY_LOW_THRESHOLD = intPreferencesKey("battery_low_threshold")
        
        private val BATTERY_CHARGED_ENABLED = booleanPreferencesKey("battery_charged_enabled")
        private val BATTERY_CHARGED_TEXT = stringPreferencesKey("battery_charged_text")
        private val BATTERY_CHARGED_THRESHOLD = intPreferencesKey("battery_charged_threshold")

        private val LAST_TRIGGERED_PREFIX = "last_triggered_"
    }

    val eventsFlow: Flow<List<BatteryEvent>> = context.dataStore.data.map { preferences ->
        listOf(
            BatteryEvent(
                id = "charger_connected",
                title = "Charger Connected",
                description = "Device charger connected",
                defaultTtsText = "Charger connected",
                ttsText = preferences[CHARGER_CONNECTED_TEXT] ?: "Charger connected",
                isEnabled = preferences[CHARGER_CONNECTED_ENABLED] ?: false,
                lastTriggered = preferences[longPreferencesKey(LAST_TRIGGERED_PREFIX + "charger_connected")] ?: 0L
            ),
            BatteryEvent(
                id = "charger_disconnected",
                title = "Charger Disconnected",
                description = "Device charger disconnected",
                defaultTtsText = "Charger disconnected",
                ttsText = preferences[CHARGER_DISCONNECTED_TEXT] ?: "Charger disconnected",
                isEnabled = preferences[CHARGER_DISCONNECTED_ENABLED] ?: false,
                lastTriggered = preferences[longPreferencesKey(LAST_TRIGGERED_PREFIX + "charger_disconnected")] ?: 0L
            ),
            BatteryEvent(
                id = "battery_low",
                title = "Battery Below 20%",
                description = "Battery percentage <= 20%",
                defaultTtsText = "Battery low",
                ttsText = preferences[BATTERY_LOW_TEXT] ?: "Battery low",
                isEnabled = preferences[BATTERY_LOW_ENABLED] ?: false,
                threshold = preferences[BATTERY_LOW_THRESHOLD] ?: 20,
                lastTriggered = preferences[longPreferencesKey(LAST_TRIGGERED_PREFIX + "battery_low")] ?: 0L
            ),
            BatteryEvent(
                id = "battery_charged",
                title = "Battery Charged to 30%",
                description = "Battery percentage >= 30%",
                defaultTtsText = "Battery charged to thirty percent",
                ttsText = preferences[BATTERY_CHARGED_TEXT] ?: "Battery charged to thirty percent",
                isEnabled = preferences[BATTERY_CHARGED_ENABLED] ?: false,
                threshold = preferences[BATTERY_CHARGED_THRESHOLD] ?: 30,
                lastTriggered = preferences[longPreferencesKey(LAST_TRIGGERED_PREFIX + "battery_charged")] ?: 0L
            )
        )
    }

    suspend fun updateLastTriggered(id: String, time: Long) {
        context.dataStore.edit { preferences ->
            preferences[longPreferencesKey(LAST_TRIGGERED_PREFIX + id)] = time
        }
    }

    suspend fun updateEvent(id: String, isEnabled: Boolean, ttsText: String) {
        context.dataStore.edit { preferences ->
            when (id) {
                "charger_connected" -> {
                    preferences[CHARGER_CONNECTED_ENABLED] = isEnabled
                    preferences[CHARGER_CONNECTED_TEXT] = ttsText
                }
                "charger_disconnected" -> {
                    preferences[CHARGER_DISCONNECTED_ENABLED] = isEnabled
                    preferences[CHARGER_DISCONNECTED_TEXT] = ttsText
                }
                "battery_low" -> {
                    preferences[BATTERY_LOW_ENABLED] = isEnabled
                    preferences[BATTERY_LOW_TEXT] = ttsText
                }
                "battery_charged" -> {
                    preferences[BATTERY_CHARGED_ENABLED] = isEnabled
                    preferences[BATTERY_CHARGED_TEXT] = ttsText
                }
            }
        }
    }
}
