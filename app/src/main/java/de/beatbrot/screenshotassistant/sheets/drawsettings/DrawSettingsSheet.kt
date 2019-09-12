package de.beatbrot.screenshotassistant.sheets.drawsettings

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.thebluealliance.spectrum.SpectrumDialog
import de.beatbrot.imagepainter.view.ImagePainterView
import de.beatbrot.screenshotassistant.R
import de.beatbrot.screenshotassistant.databinding.SheetColorsettingsBinding

class DrawSettingsSheet : Fragment() {
    private lateinit var v: SheetColorsettingsBinding

    var imagePainter: ImagePainterView? = null
        set(value) {
            field = value
            field?.setRedoStatusChangeListener {
                if (::v.isInitialized) {
                    v.redoButton.isEnabled = it
                }
            }
            field?.setUndoStatusChangeListener {
                if (::v.isInitialized) {
                    v.undoButton.isEnabled = it
                }
            }
        }

    var onHideListener: (() -> Unit)? = null

    private lateinit var colorPicker: SpectrumDialog.Builder

    private val viewModel by viewModels<DrawSettingsViewModel>()

    override fun onCreateView(i: LayoutInflater, root: ViewGroup?, state: Bundle?): View {
        v = SheetColorsettingsBinding.inflate(i, root, false)
        return v.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.strokeColor.observe(this, Observer { newValue ->
            if (newValue == null) {
                return@Observer
            }
            v.colorButton.color = newValue
            colorPicker.setSelectedColor(newValue)
            if (viewModel.editingMode.value == DrawMode.PEN) {
                imagePainter?.strokeColor = newValue
            } else {
                imagePainter?.strokeColor = ColorUtils.setAlphaComponent(newValue, 255 / 3)
            }
        })

        viewModel.strokeCap.observe(this, Observer { newValue ->
            imagePainter?.strokeCap = newValue
        })

        viewModel.strokeWidth.observe(this, Observer { newValue ->
            imagePainter?.strokeWidth = newValue
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v.undoButton.isEnabled = imagePainter?.canUndo() ?: false
        v.redoButton.isEnabled = imagePainter?.canRedo() ?: false

        colorPicker = SpectrumDialog.Builder(context).apply {
            setTitle("Pick a color")
            setDismissOnColorSelected(true)
            setColors(R.array.colorPicker)
            setOnColorSelectedListener { positiveResult, color ->
                if (positiveResult) {
                    if (viewModel.editingMode.value == DrawMode.PEN) {
                        viewModel.penColor.postValue(color)
                    } else if (viewModel.editingMode.value == DrawMode.MARKER) {
                        viewModel.markerColor.postValue(color)
                    }
                }
            }
        }

        v.undoButton.setOnClickListener { imagePainter?.undo() }
        v.redoButton.setOnClickListener { imagePainter?.redo() }
        v.penSelector.setToggled(R.id.draw_mode, true)
        v.penSelector.onToggledListener = { _, toggle, selected ->
            if (selected) {
                viewModel.editingMode.value = when (toggle.id) {
                    R.id.draw_mode -> DrawMode.PEN
                    else -> DrawMode.MARKER
                }
            }
        }

        v.doneButton.setOnClickListener { onHideListener?.invoke() }
        v.colorButton.setOnClickListener { colorPicker.show() }
    }

    private fun SpectrumDialog.Builder.show() {
        build().show(childFragmentManager, "COLOR_PICKER")
    }

    private var FloatingActionButton.color: Int
        get() = backgroundTintList?.defaultColor ?: throw NullPointerException()
        set(value) {
            backgroundTintList = ColorStateList.valueOf(value)
        }
}
