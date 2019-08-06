package de.beatbrot.screenshotassistant

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.io.FileOutputStream

const val AUTHORITY_NAME = "de.beatbrot.screenshotassistant.fileprovider"
const val MIME_TYPE = "image/jpeg"

class ScreenshotActivityViewModel(application: Application) : AndroidViewModel(application) {
    val uri: MutableLiveData<Uri> = MutableLiveData()

    private val _shareIntent: MutableLiveData<Intent> = MutableLiveData()
    val shareIntent: LiveData<Intent>
        get() = _shareIntent

    private val context: Context
        get() = getApplication<Application>().baseContext

    fun shareImage(croppedImage: Bitmap) {
        val croppedUri = getScreenshotUri(croppedImage)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, croppedUri)
        }

        _shareIntent.postValue(intent)
    }

    private fun getScreenshotUri(croppedImage: Bitmap): Uri {
        val scrFile = createScreenshotFile()

        FileOutputStream(scrFile).use { stream ->
            croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }

        return FileProvider.getUriForFile(context, AUTHORITY_NAME, scrFile)
    }

    private fun createScreenshotFile(): File {
        val scrDir = File(context.filesDir, "screenshots")
        val scrFile = File(scrDir, "scr_cropped.jpg")

        scrDir.mkdir()
        if (scrFile.exists()) {
            scrFile.delete()
        }
        scrFile.createNewFile()

        return scrFile
    }
}