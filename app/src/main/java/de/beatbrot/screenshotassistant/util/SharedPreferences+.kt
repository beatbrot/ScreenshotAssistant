package de.beatbrot.screenshotassistant.util

import android.content.SharedPreferences
import android.graphics.Bitmap

val SharedPreferences.imageQuality: Int
    get() = getInt("image_quality", 100)

val SharedPreferences.imageFormat: Bitmap.CompressFormat
    get() = Bitmap.CompressFormat.valueOf(getString("image_format", null) ?: "JPEG")
