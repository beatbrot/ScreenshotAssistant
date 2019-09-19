package de.beatbrot.screenshotassistant.custom

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import com.theartofdev.edmodo.cropper.CropImageView
import de.beatbrot.imagepainter.view.ImagePainterView
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals

fun cropImageByHalf() = object : ViewAction {
    override fun getDescription() = "Selects the top half of the image"

    override fun getConstraints(): Matcher<View> {
        return Matchers.instanceOf(CropImageView::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        if (view is CropImageView) {
            val imgRect = view.wholeImageRect
            view.cropRect = Rect(imgRect).apply { bottom /= 2 }
        }
    }
}

fun draw() = object : ViewAction {
    override fun getDescription() = "Draws on the image painter"

    override fun getConstraints() = Matchers.instanceOf<View>(ImagePainterView::class.java)

    override fun perform(uiController: UiController?, view: View?) {
        if (view is ImagePainterView) {
            view.apply {
                onTouchEvent(motionEvent(MotionEvent.ACTION_DOWN, 10, 20))
                onTouchEvent(motionEvent(MotionEvent.ACTION_MOVE, 20, 50))
                onTouchEvent(motionEvent(MotionEvent.ACTION_UP, 20, 50))
            }
        }
    }
}

fun checkCropSuccessful() = ViewAssertion { view, _ ->
    if (view is CropImageView) {
        val bitmap = view.croppedImage

        assertEquals(1920 / 2, bitmap.height)
    }
}

private fun motionEvent(action: Int, x: Int, y: Int): MotionEvent {
    return MotionEvent.obtain(1L, 1L, action, x.toFloat(), y.toFloat(), 0)
}
