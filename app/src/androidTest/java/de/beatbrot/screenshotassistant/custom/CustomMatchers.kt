package de.beatbrot.screenshotassistant.custom

import android.app.Activity
import android.content.Intent
import android.view.View
import com.theartofdev.edmodo.cropper.CropImageView
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

fun containsImage() = object : TypeSafeMatcher<View>(CropImageView::class.java) {
    override fun matchesSafely(item: View?): Boolean {
        val crop = item as CropImageView
        return crop.croppedImage.byteCount > 10
    }

    override fun describeTo(description: Description?) {
        description?.appendText("has an image set")
    }
}

fun <T : Activity> launchesActivity(activity: Class<T>) = object :
    TypeSafeMatcher<Intent>(Intent::class.java) {
    override fun matchesSafely(item: Intent?): Boolean {
        val expectedPkg = activity.`package`?.name ?: return false
        val actPkg = item?.component?.packageName ?: return false

        val expectedClass = activity.name
        val actClass = item.component?.className ?: return false

        return expectedPkg == actPkg && expectedClass == actClass
    }

    override fun describeTo(description: Description?) {
        description?.appendText("launches ${activity.name}")
    }
}
