package de.beatbrot.screenshotassistant.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

val Context.sharedPrefs: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)
