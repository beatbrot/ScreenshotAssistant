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
import de.beatbrot.screenshotassistant.databinding.ActivityMainBinding
import de.beatbrot.screenshotassistant.sheets.ModalSettingsSheet
import de.beatbrot.screenshotassistant.sheets.drawsettings.DrawSettingsSheet
import de.beatbrot.screenshotassistant.util.OpenAnimationListener
import de.beatbrot.screenshotassistant.util.tryExport

class MainActivity : AppCompatActivity() {
    private lateinit var v: ActivityMainBinding
    private val drawSettingsSheet = DrawSettingsSheet()
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
                EditingMode.CROP -> v.cropView.croppedImage
                EditingMode.PAINT -> v.imagePainter.exportImage()
                else -> throw IllegalStateException("No editing mode is set")
            }
        }
    }

    private fun initUI() {
        v = ActivityMainBinding.inflate(layoutInflater)
        setContentView(v.root)
        supportFragmentManager.beginTransaction()
            .replace(v.bottomSheet.id, drawSettingsSheet)
            .commitNow()

        drawSettingsSheet.onHideListener = { viewModel.editingMode.value = EditingMode.CROP }
        drawSettingsSheet.imagePainter = v.imagePainter

        v.cropView.setOnSetImageUriCompleteListener { view, _, _ ->
            view.cropRect = Rect(view.wholeImageRect)
        }

        v.drawButton.setOnClickListener { viewModel.editingMode.value = EditingMode.PAINT }

        v.menuButton.setOnClickListener {
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

        v.shareButton.setOnClickListener { viewModel.shareImage(v.cropView.croppedImage) }
    }

    private fun initViewModel() {
        viewModel.currentBitmap.observe(this, Observer { newBitmap ->
            when (viewModel.editingMode.value) {
                EditingMode.CROP -> {
                    v.cropView.setImageBitmap(newBitmap)
                    v.cropView.cropRect = Rect(v.cropView.wholeImageRect)
                    animateImageView(v.cropView)
                }
                EditingMode.PAINT -> {
                    v.imagePainter.setImageBitmap(newBitmap)
                    animateImageView(v.imagePainter)
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
                    v.imagePainter.visibility = View.GONE
                    v.cropView.visibility = View.VISIBLE
                    if (v.imagePainter.drawable != null) {
                        viewModel.currentBitmap.value = v.imagePainter.exportImage()
                    }
                }
                EditingMode.PAINT -> {
                    v.cropView.visibility = View.GONE
                    v.imagePainter.visibility = View.VISIBLE
                    v.cropView.tryExport()?.let {
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
        if (v.bottomSheet.visibility == View.VISIBLE) {
            return
        }

        runAnimation(v.bottomSheet, R.anim.slide_up) {
            v.bottomSheet.visibility = View.VISIBLE
        }
    }

    private fun hideBottomSheet() {
        if (v.bottomSheet.visibility != View.VISIBLE) {
            return
        }

        runAnimation(v.bottomSheet, R.anim.slide_down) {
            v.bottomSheet.visibility = View.GONE
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
