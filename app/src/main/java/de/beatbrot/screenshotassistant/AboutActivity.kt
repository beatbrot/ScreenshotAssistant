package de.beatbrot.screenshotassistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList

const val SCREENSHOT_ASSISTANT_GH = "beatbrot/ScreenshotAssistant"

const val IMAGE_CROPPER_NAME = "Android Image Cropper"
const val IMAGE_CROPPER_VERSION = "2.8.0"
const val IMAGE_CROPPER_LICENSE =
    "https://github.com/ArthurHub/Android-Image-Cropper/blob/master/LICENSE.txt"
const val IMAGE_CROPPER_GH = "ArthurHub/Android-Image-Cropper"

const val ABOUT_NAME = "material-about-library"
const val ABOUT_VERSION = "2.4.2"
const val ABOUT_LICENSE =
    "https://github.com/daniel-stoneuk/material-about-library/blob/master/LICENSE"
const val ABOUT_GH = "daniel-stoneuk/material-about-library"

class AboutActivity : MaterialAboutActivity() {
    override fun getMaterialAboutList(context: Context): MaterialAboutList {
        val mainCard = MaterialAboutCard.Builder()
            .addItem(
                MaterialAboutTitleItem(
                    R.string.app_name,
                    R.string.copyright,
                    R.mipmap.ic_launcher_round
                )
            )
            .addVersionItem(BuildConfig.VERSION_NAME)
            .addGithubItem(SCREENSHOT_ASSISTANT_GH)
            .build()

        val cropper = MaterialAboutCard.Builder()
            .title(IMAGE_CROPPER_NAME)
            .addVersionItem(IMAGE_CROPPER_VERSION)
            .addLicenseItem(R.string.apache_2, IMAGE_CROPPER_LICENSE)
            .addGithubItem(IMAGE_CROPPER_GH)
            .build()

        val aboutLib = MaterialAboutCard.Builder()
            .title(ABOUT_NAME)
            .addVersionItem(ABOUT_VERSION)
            .addLicenseItem(R.string.apache_2, ABOUT_LICENSE)
            .addGithubItem(ABOUT_GH)
            .build()

        return MaterialAboutList.Builder()
            .addCard(mainCard)
            .addCard(cropper)
            .addCard(aboutLib)
            .build()
    }

    private fun MaterialAboutCard.Builder.addVersionItem(version: String): MaterialAboutCard.Builder {
        addItem(
            MaterialAboutActionItem(
                baseContext.getString(R.string.version),
                version,
                baseContext.getDrawable(R.drawable.ic_info_outline_gray_24dp)
            )
        )
        return this
    }

    private fun MaterialAboutCard.Builder.addLicenseItem(@StringRes license: Int, url: String): MaterialAboutCard.Builder {
        addItem(
            MaterialAboutActionItem(
                R.string.license,
                license,
                R.drawable.ic_book_gray_24dp
            ) { openUrl(url) }
        )
        return this
    }

    private fun MaterialAboutCard.Builder.addGithubItem(project: String): MaterialAboutCard.Builder {
        addItem(
            MaterialAboutActionItem(
                baseContext.getString(R.string.github),
                project,
                baseContext.getDrawable(R.drawable.ic_github_gray_24dp)
            ) { openUrl("http://github.com/$project") }
        )
        return this
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun getActivityTitle() = baseContext.getString(R.string.about)
}