package de.beatbrot.screenshotassistant.voice

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.service.voice.VoiceInteractionSession
import de.beatbrot.screenshotassistant.MainActivity
import java.io.File
import java.io.FileOutputStream

class InteractionSession(context: Context) : VoiceInteractionSession(context) {
    override fun onHandleScreenshot(screenshot: Bitmap?) {
        screenshot?.let { shot ->
            val startIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("screenshot", getScreenshotUri(shot))
            }
            hide()
            context.startActivity(startIntent)
        }
    }

    private fun getScreenshotUri(bitmap: Bitmap): Uri {
        val scrFile = createScreenshotFile()

        FileOutputStream(scrFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }
        return Uri.fromFile(scrFile)
    }

    private fun createScreenshotFile(): File {
        val scrDir = File(context.cacheDir, "screenshots")
        val scrFile = File(scrDir, "scr.jpg")

        scrFile.parentFile.mkdir()
        if (scrFile.exists()) {
            scrFile.delete()
        }
        scrFile.createNewFile()

        return scrFile
    }
}