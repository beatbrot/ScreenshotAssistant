package de.beatbrot.screenshotassistant.voice

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.service.voice.VoiceInteractionSession
import android.view.View
import android.widget.Button
import androidx.core.content.FileProvider
import com.theartofdev.edmodo.cropper.CropImageView
import de.beatbrot.screenshotassistant.R
import java.io.File
import java.io.FileOutputStream


const val AUTHORITY_NAME = "de.beatbrot.screenshotassistant.fileprovider"

class InteractionSession(context: Context) : VoiceInteractionSession(context) {
    private var currentView: View? = null
    private var imageCropper: CropImageView? = null
    private var screenshot: Bitmap? = null
    private lateinit var button: Button

    override fun onCreateContentView(): View {
        val layout = layoutInflater.inflate(R.layout.activity_main, null)
        imageCropper = layout.findViewById(R.id.screenShot)
        imageCropper?.let {
            it.isAutoZoomEnabled = true
        }
        button = layout.findViewById(R.id.button)
        button.setOnClickListener { shareImage() }
        currentView = layout
        return layout
    }

    override fun onHandleScreenshot(shot: Bitmap?) {
        screenshot = shot
        imageCropper?.setImageBitmap(screenshot)
    }

    fun shareImage() {
        val screenshotUri = getScreenshotUri()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, screenshotUri)
        }

        val resInfoList =
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(
                packageName,
                screenshotUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        val chooserIntent = Intent.createChooser(intent, "Demo")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }

    private fun getScreenshotUri(): Uri {
        val scrFile = createScreenshotFile()

        FileOutputStream(scrFile).use { stream ->
            screenshot?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }

        return FileProvider.getUriForFile(context, AUTHORITY_NAME, scrFile)
    }

    private fun createScreenshotFile(): File {
        val scrDir = File(context.filesDir, "screenshots")
        val scrFile = File(scrDir, "scr_${System.currentTimeMillis()}.jpg")

        scrFile.parentFile.mkdir()
        scrFile.createNewFile()

        return scrFile
    }
}