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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import de.beatbrot.screenshotassistant.sheets.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ScreenshotActivityViewModel

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

        bottomSheet.visibility = View.GONE

        screenShot.setOnSetImageUriCompleteListener { view, _, _ ->
            view.cropRect = Rect(view.wholeImageRect)
        }

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

    private fun showSettings(): Boolean {
        val parms = bottomSheet.layoutParams as CoordinatorLayout.LayoutParams
        val behav = parms.behavior as BottomSheetBehavior
        behav.state = BottomSheetBehavior.STATE_COLLAPSED
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomContainer, SettingsFragment())
            .commit()
        bottomSheet.visibility = View.VISIBLE
        behav.state = BottomSheetBehavior.STATE_EXPANDED
        behav.isHideable = true
        return true
    }

    private fun <T : Activity> startActivity(clazz: KClass<T>): Boolean {
        startActivity(Intent(baseContext, clazz.java))
        return true
    }
}
