package de.beatbrot.screenshotassistant

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class TitledBottomSheet @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL
        isClickable = true

        val title = TextView(context).apply {
            setTextAppearance(R.style.TextAppearance_MaterialComponents_Headline6)
            text = "Fooo"
            layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).also { params ->
                    params.marginStart = 16.toPx(context)
                    params.marginEnd = 16.toPx(context)
                    params.topMargin = 16.toPx(context)
                    params.bottomMargin = 16.toPx(context)
                }
        }

        addView(title)
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        println("Foo")
    }

    private fun Int.toPx(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}