package de.beatbrot.screenshotassistant

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import de.beatbrot.screenshotassistant.sheets.DrawSettingsSheet
import de.beatbrot.screenshotassistant.sheets.IBottomSheet
import de.beatbrot.screenshotassistant.sheets.ModalSettingsSheet
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ScreenshotActivityViewModel

    private val drawSettingsSheet = DrawSettingsSheet().apply {
        onHideListener = {
            switchState()
        }
    }

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
        val params = bottomSheet.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as BottomSheetBehavior
        behavior.state = BottomSheetBehavior.STATE_HIDDEN

        screenShot.setOnSetImageUriCompleteListener { view, _, _ ->
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

        button.setOnClickListener { viewModel.shareImage(screenShot.croppedImage) }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this)[ScreenshotActivityViewModel::class.java]

        viewModel.uri.observe(this, Observer { newUri ->
            screenShot.setImageUriAsync(newUri)
            animateImageView()
        })

        viewModel.shareIntent.observe(this, Observer { intent ->
            startActivity(Intent.createChooser(intent, baseContext.getString(R.string.share_image)))
        })

        viewModel.editingMode.observe(this, Observer { newState ->
            when (newState) {
                EditingMode.CROP -> {
                    supportFragmentManager.beginTransaction()
                        .remove(drawSettingsSheet)
                        .commit()
                    imagePainter.visibility = View.INVISIBLE
                    screenShot.visibility = View.VISIBLE
                    if (imagePainter.drawable != null) {
                        screenShot.setImageBitmap(imagePainter.exportImage())
                        screenShot.cropRect = Rect(screenShot.wholeImageRect)
                    }
                }
                EditingMode.PAINT -> {
                    screenShot.visibility = View.INVISIBLE
                    imagePainter.visibility = View.VISIBLE
                    imagePainter.setImageBitmap(screenShot.croppedImage)
                    showDrawSettings()
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
        screenShot?.apply {
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

    private fun showDrawSettings(): Boolean {
        showBottomSheet(drawSettingsSheet)
        drawSettingsSheet.imagePainter = imagePainter
        return true
    }

    private fun showSettings(): Boolean {
        ModalSettingsSheet().showNow(supportFragmentManager, "SETTINGS")
        return true
    }

    private fun <T> showBottomSheet(sheet: T)
            where T : Fragment, T : IBottomSheet {
        val params = bottomSheet.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as BottomSheetBehavior
        behavior.isHideable = true

        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomContainer, sheet)
            .commitNow()

        if (sheet.title != null) {
            bottomSheetHeader.visibility = View.VISIBLE
            bottomSheetHeader.text = sheet.title
        } else {
            bottomSheetHeader.visibility = View.GONE
        }
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun <T : Activity> startActivity(clazz: KClass<T>): Boolean {
        startActivity(Intent(baseContext, clazz.java))
        return true
    }
}
