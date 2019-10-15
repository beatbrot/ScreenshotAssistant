package de.beatbrot.screenshotassistant.sheets.drawsettings

import android.graphics.Color
import android.graphics.Paint
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import de.beatbrot.screenshotassistant.util.liveDataOf

class DrawSettingsViewModel : ViewModel() {
    val editingMode = liveDataOf(DrawMode.PEN)

    val penColor = liveDataOf(Color.BLACK)
    val markerColor = liveDataOf(-0x14C5) // Yellow

    val strokeColor = MediatorLiveData<Int>().apply {
        addSource(penColor) { newValue ->
            if (editingMode.value == DrawMode.PEN) {
                value = newValue
            }
        }
        addSource(markerColor) { newValue ->
            if (editingMode.value == DrawMode.MARKER) {
                value = newValue
            }
        }
        addSource(editingMode) { drawMode ->
            value = when (drawMode) {
                DrawMode.PEN -> {
                    penColor.value
                }
                DrawMode.MARKER -> {
                    markerColor.value
                }
                else -> value
            }
        }
    }

    val strokeCap = MediatorLiveData<Paint.Cap>().apply {
        addSource(editingMode) { newMode ->
            value = when (newMode) {
                DrawMode.PEN -> Paint.Cap.ROUND
                DrawMode.MARKER -> Paint.Cap.SQUARE
                else -> value
            }
        }
    }

    val strokeWidth = MediatorLiveData<Float>().apply {
        addSource(editingMode) { newMode ->
            value = when (newMode) {
                DrawMode.PEN -> 3F
                DrawMode.MARKER -> 30F
                else -> value
            }
        }
    }
}
