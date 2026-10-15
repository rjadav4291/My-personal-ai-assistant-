package com.chintu.ai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.getDefault()
            isInitialized = true
        }
    }

    fun speak(text: String, onComplete: () -> Unit = {}) {
        if (isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "CHINTU_TTS")
            onComplete()
        }
    }

    fun stop() {
        if (isInitialized) {
            tts.stop()
        }
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
