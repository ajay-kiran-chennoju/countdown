package com.example.progresswidget.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.progresswidget.data.BatteryDataStore
import com.example.progresswidget.model.BatteryEvent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class BatteryTrackerViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val dataStore = BatteryDataStore(application)
    private var tts: TextToSpeech? = TextToSpeech(application, this)

    val events: StateFlow<List<BatteryEvent>> = dataStore.eventsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateEvent(id: String, isEnabled: Boolean, ttsText: String) {
        viewModelScope.launch {
            dataStore.updateEvent(id, isEnabled, ttsText)
        }
    }

    fun testVoice(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
