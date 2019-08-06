package de.beatbrot.screenshotassistant

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ScreenshotActivityViewModel
    private lateinit var origImage: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        initViewModel()

        origImage = intent.getParcelableExtra("screenshot")
            ?: throw IllegalArgumentException("No screenshot provided to activity")

        viewModel.uri.postValue(origImage)
    }

    private fun initUI() {
        setContentView(R.layout.activity_main)

        screenShot.setOnSetImageUriCompleteListener { view, _, _ ->
            view.cropRect = Rect(view.wholeImageRect)
        }

        menuButton.setOnClickListener {
            val popMenu = PopupMenu(baseContext, it)
            popMenu.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.about_item) {
                    startActivity(Intent(baseContext, AboutActivity::class.java))
                    //startAboutMenu()
                }
                true
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
        })

        viewModel.shareIntent.observe(this, Observer { intent ->
            startActivity(Intent.createChooser(intent, "Sharing..."))
        })
    }
}
