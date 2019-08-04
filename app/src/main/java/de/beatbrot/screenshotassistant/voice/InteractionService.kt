package de.beatbrot.screenshotassistant.voice

import android.service.voice.VoiceInteractionService
import android.util.Log

class InteractionService : VoiceInteractionService() {
    init {
        Log.e("FOO", "initing interaction")
    }
}

