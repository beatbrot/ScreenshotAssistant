package de.beatbrot.screenshotassistant

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList

class AboutActivity : MaterialAboutActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isNightModeActive) {
            setTheme(R.style.AppTheme_AboutDialog_Dark)
        } else {
            setTheme(R.style.AppTheme_AboutDialog)
        }
        super.onCreate(savedInstanceState)
    }

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
            .addGithubItem("beatbrot/ScreenshotAssistant")
            .build()

        return MaterialAboutList.Builder()
            .addCard(mainCard)
            .addCard(ImageCropper.createCard())
            .addCard(MaterialAboutLibrary.createCard())
            .build()
    }

    private val isNightModeActive: Boolean
        get() {
            val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return uiMode == Configuration.UI_MODE_NIGHT_YES
        }

    interface Library {
        val name: String
        val version: String

        @get:StringRes
        val license: Int

        val licenseLink: String

        val github: String
    }

    object ImageCropper : Library {
        override val name = "Android Image Cropper"
        override val version = "2.8.0"

        override val license = R.string.apache_2
        override val licenseLink =
            "https://github.com/ArthurHub/Android-Image-Cropper/blob/master/LICENSE.txt"

        override val github = "ArthurHub/Android-Image-Cropper"
    }

    object MaterialAboutLibrary : Library {
        override val name = "material-about-library"
        override val version = "2.4.2"
        override val license = R.string.apache_2
        override val licenseLink =
            "https://github.com/daniel-stoneuk/material-about-library/blob/master/LICENSE"
        override val github = "daniel-stoneuk/material-about-library"
    }

    private fun Library.createCard() = MaterialAboutCard.Builder()
        .title(name)
        .addVersionItem(version)
        .addLicenseItem(license, licenseLink)
        .addGithubItem(github)
        .build()

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

    override fun getActivityTitle(): String = baseContext.getString(R.string.about)
}
