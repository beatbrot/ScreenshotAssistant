package de.beatbrot.screenshotassistant.util

import android.graphics.Bitmap
import java.util.*

val Enum<Bitmap.CompressFormat>.mimeType
    get() = "image/${name.toLowerCase(Locale.ROOT)}"

val Enum<Bitmap.CompressFormat>.fileExtension
    get() = name.toLowerCase(Locale.ROOT)
