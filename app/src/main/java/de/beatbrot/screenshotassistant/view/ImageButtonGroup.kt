package de.beatbrot.screenshotassistant.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import de.beatbrot.screenshotassistant.R


class ImageButtonGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var selectedElement: View? = null
        set(value) {
            field?.isSelected = false
            field = value
            field?.isSelected = true
            if (value != null) {
                onSelectListener?.invoke(value)
            }
        }

    var onSelectListener: ((View) -> Unit)? = null

    private val clickHandler = ClickHandler()

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 1) {
            val first = getChildAt(0)
            selectedElement = first
        }
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        child.background = context.getDrawable(R.drawable.toggle_image_button_bg)
        child.setOnClickListener(clickHandler)
    }

    private inner class ClickHandler : OnClickListener {
        override fun onClick(v: View) {
            selectedElement = v
        }

    }
}