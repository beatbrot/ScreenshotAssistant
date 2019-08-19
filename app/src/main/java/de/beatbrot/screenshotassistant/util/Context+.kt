package de.beatbrot.screenshotassistant.util

import android.content.Context
import androidx.preference.PreferenceManager

val Context.sharedPrefs
    get() = PreferenceManager.getDefaultSharedPreferences(this)