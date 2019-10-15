package de.beatbrot.screenshotassistant.util

import android.graphics.Bitmap
import com.theartofdev.edmodo.cropper.CropImageView

fun CropImageView.tryExport(): Bitmap? {
    return try {
        croppedImage
    } catch (exception: Exception) {
        null
    }
}
