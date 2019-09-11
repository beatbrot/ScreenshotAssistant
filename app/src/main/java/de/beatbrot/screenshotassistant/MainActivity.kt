package de.beatbrot.screenshotassistant

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.AnimRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import de.beatbrot.screenshotassistant.sheets.ModalSettingsSheet
import de.beatbrot.screenshotassistant.sheets.drawsettings.DrawSettingsSheet
import de.beatbrot.screenshotassistant.util.OpenAnimationListener
import de.beatbrot.screenshotassistant.util.tryExport
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val viewModel by viewModels<ScreenshotActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        initViewModel()

        val imgPath = intent.getParcelableExtra<Uri>("screenshot")
        if (imgPath != null) {
            loadInitialImage(imgPath)
        } else {
            Toast.makeText(baseContext, R.string.error_no_screenshot, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isFinishing) {
            viewModel.currentBitmap.value = when (viewModel.editingMode.value) {
                EditingMode.CROP -> cropView.croppedImage
                EditingMode.PAINT -> imagePainter.exportImage()
                else -> throw IllegalStateException("No editing mode is set")
            }
        }
    }

    private fun initUI() {
        (drawSheet as DrawSettingsSheet).let { sheet ->
            sheet.onHideListener = { viewModel.editingMode.value = EditingMode.CROP }
            sheet.imagePainter = imagePainter
        }

        cropView.setOnSetImageUriCompleteListener { view, _, _ ->
            view.cropRect = Rect(view.wholeImageRect)
        }

        drawButton.setOnClickListener { viewModel.editingMode.value = EditingMode.PAINT }

        menuButton.setOnClickListener {
            val popMenu = PopupMenu(baseContext, it)
            popMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.settings_item -> showSettings()
                    R.id.about_item -> startActivity(Intent(baseContext, AboutActivity::class.java))
                    else -> throw IllegalArgumentException()
                }
                true
            }
            popMenu.menuInflater.inflate(R.menu.about_menu, popMenu.menu)
            popMenu.show()
        }

        shareButton.setOnClickListener { viewModel.shareImage(cropView.croppedImage) }
    }

    private fun initViewModel() {
        viewModel.currentBitmap.observe(this, Observer { newBitmap ->
            when (viewModel.editingMode.value) {
                EditingMode.CROP -> {
                    cropView.setImageBitmap(newBitmap)
                    cropView.cropRect = Rect(cropView.wholeImageRect)
                    animateImageView(cropView)
                }
                EditingMode.PAINT -> {
                    imagePainter.setImageBitmap(newBitmap)
                    animateImageView(imagePainter)
                }
            }
        })

        viewModel.shareIntent.observe(this, Observer { intent ->
            startActivity(Intent.createChooser(intent, baseContext.getString(R.string.share_image)))
        })

        viewModel.editingMode.observe(this, Observer { newState ->
            when (newState) {
                EditingMode.CROP -> {
                    hideBottomSheet()
                    imagePainter.visibility = View.GONE
                    cropView.visibility = View.VISIBLE
                    if (imagePainter.drawable != null) {
                        viewModel.currentBitmap.value = imagePainter.exportImage()
                    }
                }
                EditingMode.PAINT -> {
                    cropView.visibility = View.GONE
                    imagePainter.visibility = View.VISIBLE
                    cropView.tryExport()?.let {
                        viewModel.currentBitmap.value = it
                    }
                    showBottomSheet()
                }
                else -> throw UnsupportedOperationException()
            }
        })
    }

    private fun animateImageView(imageView: View) {
        val startValue = 1.3F
        imageView.apply {
            scaleX = startValue
            scaleY = startValue
            alpha = 0.5F

            visibility = View.VISIBLE

            animate()
                .scaleX(1F)
                .scaleY(1F)
                .alpha(1F)
                .setInterpolator(OvershootInterpolator())
                .start()
        }
    }

    private fun showBottomSheet() {
        if (bottomSheet.visibility == View.VISIBLE) {
            return
        }

        runAnimation(bottomSheet, R.anim.slide_up) {
            bottomSheet.visibility = View.VISIBLE
        }
    }

    private fun hideBottomSheet() {
        if (bottomSheet.visibility != View.VISIBLE) {
            return
        }

        runAnimation(bottomSheet, R.anim.slide_down) {
            bottomSheet.visibility = View.GONE
        }
    }

    private inline fun runAnimation(
        view: View, @AnimRes animRes: Int,
        crossinline onDone: () -> Unit
    ) {
        val anim = AnimationUtils.loadAnimation(baseContext, animRes)
        anim.setAnimationListener(object : OpenAnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                onDone()
            }
        })
        view.startAnimation(anim)
    }

    private fun showSettings() {
        ModalSettingsSheet().showNow(supportFragmentManager, "SETTINGS")
    }

    private fun loadInitialImage(uri: Uri) {
        if (viewModel.currentBitmap.value == null) {
            val inputStream = contentResolver.openInputStream(uri)
            viewModel.currentBitmap.value = BitmapFactory.decodeStream(inputStream)
        }
    }
}
