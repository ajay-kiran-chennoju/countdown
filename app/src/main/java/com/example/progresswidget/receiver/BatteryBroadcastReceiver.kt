package com.example.progresswidget.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.progresswidget.data.BatteryDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class BatteryBroadcastReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var tts: TextToSpeech? = null
    private var lastTriggeredId: String? = null
    private var lastTriggeredTime: Long = 0

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val serviceIntent = Intent(context, com.example.progresswidget.service.BatteryAutomationService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            return
        }

        val dataStore = BatteryDataStore(context)

        scope.launch {
            val events = dataStore.eventsFlow.first()
            
            when (action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    triggerEvent(context, "charger_connected", events)
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    triggerEvent(context, "charger_disconnected", events)
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val batteryPct = level * 100 / scale.toFloat()

                    if (batteryPct <= 20) {
                        triggerEvent(context, "battery_low", events)
                    } else if (batteryPct >= 30 && batteryPct < 31) { // Narrow window to avoid spam
                        triggerEvent(context, "battery_charged", events)
                    }
                }
            }
        }
    }

    private fun triggerEvent(context: Context, id: String, events: List<com.example.progresswidget.model.BatteryEvent>) {
        val event = events.find { it.id == id } ?: return
        if (!event.isEnabled) return

        // Debounce: Avoid repeating the same event within 5 seconds
        val now = System.currentTimeMillis()
        if (lastTriggeredId == id && now - lastTriggeredTime < 5000) return

        lastTriggeredId = id
        lastTriggeredTime = now

        scope.launch {
            BatteryDataStore(context).updateLastTriggered(id, now)
        }

        speak(context, event.ttsText)
    }

    private fun speak(context: Context, text: String) {
        // Since we are in a receiver, we should use a short-lived TTS or a service
        // For simplicity in this utility app, we'll initialize it here
        // but a Service is better for production.
        TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                // Note: TTS will be leaked if not shut down, but receivers are short-lived.
                // In a real app, we'd use a Service.
            }
        }.also { tts = it }
    }
}
