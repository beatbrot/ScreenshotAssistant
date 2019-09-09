package de.beatbrot.screenshotassistant

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.beatbrot.screenshotassistant.sheets.ModalSettingsSheet
import de.beatbrot.screenshotassistant.sheets.drawsettings.DrawSettingsSheet
import de.beatbrot.screenshotassistant.util.OpenAnimationListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<ScreenshotActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        initViewModel()

        val imgPath = intent.getParcelableExtra<Uri>("screenshot")
        if (imgPath != null) {
            viewModel.uri.postValue(imgPath)
        } else {
            Toast.makeText(baseContext, R.string.error_no_screenshot, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initUI() {
        setContentView(R.layout.activity_main)

        (drawSheet as DrawSettingsSheet).let { sheet ->
            sheet.onHideListener = { switchState() }
            sheet.imagePainter = imagePainter
        }

        cropView.setOnSetImageUriCompleteListener { view, _, _ ->
            view.cropRect = Rect(view.wholeImageRect)
        }

        drawButton.setOnClickListener { switchState() }

        menuButton.setOnClickListener {
            val popMenu = PopupMenu(baseContext, it)
            popMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.settings_item -> showSettings()
                    R.id.about_item -> startActivity(AboutActivity::class)
                    else -> false
                }
            }
            popMenu.menuInflater.inflate(R.menu.about_menu, popMenu.menu)
            popMenu.show()
        }

        shareButton.setOnClickListener { viewModel.shareImage(cropView.croppedImage) }
    }

    private fun initViewModel() {
        viewModel.uri.observe(this, Observer { newUri ->
            cropView.setImageUriAsync(newUri)
            animateImageView()
        })

        viewModel.shareIntent.observe(this, Observer { intent ->
            startActivity(Intent.createChooser(intent, baseContext.getString(R.string.share_image)))
        })

        viewModel.editingMode.observe(this, Observer { newState ->
            when (newState) {
                EditingMode.CROP -> {
                    hideBottomSheet()
                    imagePainter.visibility = View.INVISIBLE
                    cropView.visibility = View.VISIBLE
                    if (imagePainter.drawable != null) {
                        cropView.setImageBitmap(imagePainter.exportImage())
                        cropView.cropRect = Rect(cropView.wholeImageRect)
                    }
                }
                EditingMode.PAINT -> {
                    cropView.visibility = View.INVISIBLE
                    imagePainter.visibility = View.VISIBLE
                    imagePainter.setImageBitmap(cropView.croppedImage)
                    showBottomSheet()
                }
                else -> throw UnsupportedOperationException()
            }
        })
    }

    private fun switchState() {
        if (viewModel.editingMode.value == EditingMode.CROP) {
            viewModel.editingMode.postValue(EditingMode.PAINT)
        } else {
            viewModel.editingMode.postValue(EditingMode.CROP)
        }
    }

    private fun animateImageView() {
        val startValue = 1.3F
        cropView?.apply {
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

        val anim = AnimationUtils.loadAnimation(baseContext, R.anim.slide_up)
        anim.setAnimationListener(object : OpenAnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                bottomSheet.visibility = View.VISIBLE
            }
        })
        bottomSheet.startAnimation(anim)
    }

    private fun hideBottomSheet() {
        if (bottomSheet.visibility != View.VISIBLE) {
            return
        }

        val anim = AnimationUtils.loadAnimation(baseContext, R.anim.slide_down)
        anim.setAnimationListener(object : OpenAnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                bottomSheet.visibility = View.GONE
            }
        })
        bottomSheet.startAnimation(anim)
    }

    private fun showSettings(): Boolean {
        ModalSettingsSheet().showNow(supportFragmentManager, "SETTINGS")
        return true
    }

    private fun <T : Activity> startActivity(clazz: KClass<T>): Boolean {
        startActivity(Intent(baseContext, clazz.java))
        return true
    }
}
